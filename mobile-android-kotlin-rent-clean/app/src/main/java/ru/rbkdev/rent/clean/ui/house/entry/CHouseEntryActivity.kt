package ru.rbkdev.rent.clean.ui.house.entry

import ru.rbkdev.rent.clean.*
import ru.rbkdev.rent.clean.CSettings
import ru.rbkdev.rent.clean.tools.sendToOriginalThread
import ru.rbkdev.rent.clean.bluetooth.CBluetoothScan
import ru.rbkdev.rent.clean.bluetooth.CBluetoothLeService
import ru.rbkdev.rent.clean.room.database.keys.CKeysTable
import ru.rbkdev.rent.clean.ui.house.control.CHouseCtrlActivity
import ru.rbkdev.rent.clean.room.CDatabaseViewModel
import ru.rbkdev.rent.clean.ui.house.control.CControl
import ru.rbkdev.rent.clean.ui.house.control.Operation

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider

import java.util.*

/***/
enum class CStatus {

    /***/
    NULL,

    /***/
    COMMAND_WAIT_USER,

    /***/
    COMMAND_LEARNING_MODE,

    /***/
    COMMAND_GET_LOCK_KEY,

    /***/
    COMMAND_LOCK_OPEN,

    /***/
    COMMAND_SAVE_USER,

    /***/
    REGISTRATION,
}

/***/
data class CCommand(

    /***/
    val address: String,

    /***/
    val cmd: List<Byte>
)

/***/
data class CDataBle(

    /***/
    var name: String,

    /***/
    var address: String,

    /***/
    var state: Int,
)

/***/
class CHouseEntryActivity : AppCompatActivity() {

    /** BACKEND */
    // -------------------------------------------------------------------

    private var mViewModelDatabase: CDatabaseViewModel? = null

    /** List ble connection */
    private lateinit var mLock: CDataBle
    private lateinit var mBridge: CDataBle
    private lateinit var mListBle: MutableList<CDataBle>

    private var mLoop: Boolean = true
    private var mConnectDevice = ""

    /***/
    private val mControl: CControl = CControl()

    /** Queue commands */
    private val mCommandsQueue: Queue<CCommand> = ArrayDeque()

    /** Bluetooth service */
    private var mBluetoothService: CBluetoothLeService? = null

    private lateinit var mResultLauncher: ActivityResultLauncher<Intent>

    /** FRONTEND */
    // -------------------------------------------------------------------

    private lateinit var mHouse: CKeysTable
    private lateinit var mTxtHouseEntryDebugStatus: TextView
    private lateinit var mTxtHouseEntryStatus: TextView

    companion object {

        /***/
        const val BRIDGE_STATUS_WAIT_USER: String = "WAIT_USER"

        /***/
        const val BRIDGE_STATUS_SAVE_USER: String = "SAVE_USER"
    }

    /***/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.house_entry_activity)

        /** Settings view module database */
        mViewModelDatabase = ViewModelProvider(this).get(CDatabaseViewModel::class.java)

        mTxtHouseEntryStatus = findViewById(R.id.txtHouseEntryStatus)
        mTxtHouseEntryDebugStatus = findViewById(R.id.txtHouseEntryDebugStatus)

        mLock = CDataBle("Замок", "", CBluetoothLeService.STATE_DISCONNECT)
        mBridge = CDataBle("Мост", "", CBluetoothLeService.STATE_DISCONNECT)
        mListBle = mutableListOf(mLock, mBridge)

        mHouse = intent.getSerializableExtra(resources.getString(R.string.intent_house)) as CKeysTable

        val gattServiceIntent = Intent(this, CBluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

        /** Activity result -> return ActivityResultLauncher<Intent> */
        mResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            finish()
        }
    }

    /***/
    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter().apply {

            addAction(CBluetoothLeService.ACTION_DATA_READ)
            addAction(CBluetoothLeService.ACTION_DATA_WRITE)
            addAction(CBluetoothLeService.ACTION_DATA_AVAILABLE)
            addAction(CBluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(CBluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(CBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        }

        /** Register receiver */
        registerReceiver(mGattUpdateReceiver, intentFilter)

        Thread { scanLoop() }.start()
    }

    /***/
    override fun onPause() {
        super.onPause()

        /** Unregister receiver */
        unregisterReceiver(mGattUpdateReceiver)

        mLoop = false
    }

    /** Interface service */
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {

            mBluetoothService = (service as CBluetoothLeService.LocalBinder).getService()
            mBluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize())
                    finish()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothService = null
        }
    }

    /** Callback service */
    private val mGattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val current = intent.getStringExtra("EXTRA_ADDRESS")
            if (current != null) {
                for (item in mListBle) {
                    if (item.address == current) {

                        when (intent.action) {

                            CBluetoothLeService.ACTION_DATA_AVAILABLE -> {

                                var dataString = ""
                                // val currentStatus = mHouse.status

                                intent.getByteArrayExtra("EXTRA_DATA")?.let { array ->
                                    dataString = array.toString(Charsets.UTF_8)

                                    if (mLock.address == current) {

                                        if (dataString[0] == '<' && dataString[1] == '#' || dataString[0] == '>') {

                                            if (mHouse.status < CStatus.COMMAND_LOCK_OPEN) {

                                                mControl.receivedData(array, "")

                                                if (CSettings.checkFileInit("myappkey", mLock.address)) {

                                                    mHouse.status = CStatus.COMMAND_LOCK_OPEN

                                                    sendToOriginalThread { mTxtHouseEntryStatus.text = "Регистрация пользователя" }

                                                    mCommandsQueue.poll() // Не удаляется в общем цикле
                                                }
                                            }
                                        }

                                        if (dataString.contains("<DAS>")) {

                                            if (mHouse.status < CStatus.COMMAND_GET_LOCK_KEY) {
                                                mHouse.status = CStatus.COMMAND_GET_LOCK_KEY

                                                sendToOriginalThread { mTxtHouseEntryStatus.text = "Получение ключа" }

                                                mCommandsQueue.poll() // Не успевает удалиться

                                                Thread.sleep(4000)

                                                val message = mControl.createCommand(Operation.KEY, "")
                                                mCommandsQueue.add(CCommand(mLock.address, message.asList()))
                                            }
                                        }

                                        if (dataString.contains("<YWU>") || dataString.contains("<JUS>")) {

                                            if (mHouse.status < CStatus.COMMAND_SAVE_USER) {
                                                mHouse.status = CStatus.COMMAND_SAVE_USER

                                                sendToOriginalThread { mTxtHouseEntryStatus.text = "Завершение регистрации" }

                                                // mCommandsQueue.clear() // Оставшиеся посылки на замок

                                                mBluetoothService?.disconnect(mLock.address)

                                                addCommandBridge("registration_finish")
                                            }
                                        }
                                    }

                                    if (mBridge.address == current) {

                                        if (dataString.contains("<") && dataString.contains(">")) {

                                            if (mHouse.status < CStatus.COMMAND_LEARNING_MODE) {
                                                mHouse.status = CStatus.COMMAND_LEARNING_MODE

                                                sendToOriginalThread { mTxtHouseEntryStatus.text = "Добавление пользователя" }

                                                mBluetoothService?.disconnect(mBridge.address)

                                                mConnectDevice = mLock.address
                                                mCommandsQueue.add(CCommand(mLock.address, dataString.toByteArray().toList()))
                                            }
                                        }

                                        if (dataString == BRIDGE_STATUS_WAIT_USER) {

                                            if (mHouse.status < CStatus.COMMAND_WAIT_USER) {
                                                mHouse.status = CStatus.COMMAND_WAIT_USER

                                                sendToOriginalThread { mTxtHouseEntryStatus.text = "Начало регистрации" }

                                                addCommandBridge("registration_start")
                                            }
                                        }

                                        if (dataString == BRIDGE_STATUS_SAVE_USER) {

                                            if (mHouse.status < CStatus.REGISTRATION) {
                                                mHouse.status = CStatus.REGISTRATION

                                                sendToOriginalThread { mTxtHouseEntryStatus.text = "Успех!" }

                                                mBluetoothService?.disconnect(mBridge.address)
                                                mBluetoothService?.disconnect(mLock.address)

                                                mViewModelDatabase?.update(baseContext, mHouse)

                                                val intentAddress = Intent(baseContext, CHouseCtrlActivity::class.java)
                                                intentAddress.putExtra(baseContext.getString(R.string.intent_address), mLock.address)
                                                mResultLauncher.launch(intentAddress)
                                            }
                                        }
                                    }
                                }

                                // if (currentStatus != mHouse.status)
                                    sendToOriginalThread { mTxtHouseEntryDebugStatus.append("${item.name}: сообщение пришло: $dataString\n") }
                            }

                            CBluetoothLeService.ACTION_GATT_CONNECTED -> {

                                item.state = CBluetoothLeService.STATE_CONNECTED
                                sendToOriginalThread { mTxtHouseEntryDebugStatus.append("${item.name}: соединение\n") }
                            }

                            CBluetoothLeService.ACTION_GATT_DISCONNECTED -> {

                                item.state = CBluetoothLeService.STATE_DISCONNECT
                                sendToOriginalThread { mTxtHouseEntryDebugStatus.append("${item.name}: рассоединение\n") }
                            }

                            CBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {

                                item.state = CBluetoothLeService.STATE_CONNECT
                                sendToOriginalThread { mTxtHouseEntryDebugStatus.append("${item.name}: открыт\n") }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun scanLoop() {

        var exitTimeout = 0

        sendToOriginalThread { mTxtHouseEntryDebugStatus.append("Поиск устройств\n") }
        sendToOriginalThread { mTxtHouseEntryStatus.text = "Поиск устройства" }

        CBluetoothScan.getInstance().startScan(baseContext)

        while (mLoop) {

            /** Scan ble device */

            if (CBluetoothScan.getInstance().getDiscovering()) {
                sendToOriginalThread { mTxtHouseEntryDebugStatus.append(".\n") }
                Thread.sleep(1000L)
                exitTimeout++
            }

            if (exitTimeout == 10) {
                sendToOriginalThread { Toast.makeText(baseContext, "Тайм-аут", Toast.LENGTH_LONG).show() }
                CBluetoothScan.getInstance().stopScan()
                mLoop = false
                finish()
            }

            if (mLock.address.isEmpty()) {
                mLock.address = CBluetoothScan.getInstance().getAddressLock()
                if (mLock.address.isNotEmpty()) {
                    sendToOriginalThread { mTxtHouseEntryDebugStatus.append("${mLock.name}: найден\n") }
                    mHouse.addressLock = mLock.address
                }
            }

            if (mBridge.address.isEmpty()) {
                mBridge.address = CBluetoothScan.getInstance().getAddressBridge()
                if (mBridge.address.isNotEmpty()) {
                    sendToOriginalThread { mTxtHouseEntryDebugStatus.append("${mBridge.name}: найден\n") }
                    mHouse.addressBridge = mBridge.address

                    mConnectDevice = mBridge.address
                }
            }

            mBluetoothService?.let { bluetoothService ->

                if (mLock.address.isNotEmpty() && mBridge.address.isNotEmpty()) {
                    if (CBluetoothScan.getInstance().getDiscovering()) {
                        CBluetoothScan.getInstance().stopScan()
                        sendToOriginalThread { mTxtHouseEntryStatus.text = "Ожидание команды" }
                    }

                    /** Connect ble device */
                    for (itemBle in mListBle)
                        if (itemBle.address == mConnectDevice && itemBle.state == CBluetoothLeService.STATE_DISCONNECT)
                            if (bluetoothService.connect(itemBle.address))
                                itemBle.state = CBluetoothLeService.STATE_CONNECTED

                    /** Write ble device */
                    if (!mCommandsQueue.isEmpty()) {
                        val command = mCommandsQueue.element()
                        for ((name, address, state) in mListBle) {
                            if (address == command.address) {
                                if (state == CBluetoothLeService.STATE_CONNECT) {
                                    if (bluetoothService.writeCharacteristic(command.cmd.toByteArray(), address)) {

                                        val dataString = command.cmd.toByteArray().toString(Charsets.UTF_8)
                                        sendToOriginalThread { mTxtHouseEntryDebugStatus.append("${name}: сообщение отправлено: $dataString\n") }

                                        // sendToOriginalThread { mTxtHouseEntryDebugStatus.append("${name}: сообщение отправлено\n") }

                                        if (mHouse.status != CStatus.COMMAND_GET_LOCK_KEY)
                                            mCommandsQueue.poll()

                                        if (mHouse.status == CStatus.COMMAND_GET_LOCK_KEY)
                                            Thread.sleep(2000L)

                                        if (mHouse.status == CStatus.COMMAND_LOCK_OPEN)
                                            Thread.sleep(2000L)

                                        //if (mHouse.status == CStatus.COMMAND_SAVE_USER)
                                        //  Thread.sleep(2000L)
                                    }
                                }
                            }
                        }
                    } else {


                        if (mHouse.status == CStatus.COMMAND_LOCK_OPEN) {

                            try {

                                val message: ByteArray = mControl.createCommand(Operation.UNLOCK, "")
                                mCommandsQueue.add(CCommand(mLock.address, message.asList()))
                            } catch (e: Exception) {
                                print(e.toString())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addCommandBridge(message: String) {

        mConnectDevice = mBridge.address

        val date = CDataExchange()
        date.setCommand("phone_bridge_server")
        date.setStatus("success")
        date.setIdHouse(mHouse.idHouse)
        date.setCodePassword(mHouse.codePassword)
        date.setMessage(message)
        val arraySend = CClient.collectMessage(baseContext, date)
        mCommandsQueue.add(CCommand(mBridge.address, arraySend.toByteArray().asList()))
    }
}