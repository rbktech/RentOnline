package ru.rbkdev.rent.ui.house.entry

import ru.rbkdev.rent.R

import ru.rbkdev.rent.CSettings
import ru.rbkdev.rent.tools.sendToOriginalThread
import ru.rbkdev.rent.bluetooth.CBluetoothLeService
import ru.rbkdev.rent.room.database.keys.CKeysTable
import ru.rbkdev.rent.ui.house.control.CHouseCtrlActivity
import ru.rbkdev.rent.bluetooth.CBluetoothScanV
import ru.rbkdev.rent.ui.house.control.CControl
import ru.rbkdev.rent.ui.house.control.Operation

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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

object CFile {

    fun read(filename: String): String {

        val file: File = File(filename)
        if(file.exists())
            return file.readText()
        return ""
    }

    fun write(filename: String, data: String) {

        File(filename).printWriter().use { out ->
            out.println(data)
        }
    }

    fun load(path: String) : CKeysTable {

        val keysTable = CKeysTable()

        keysTable.idHouse = read("$path/userkey")
        var counter = read("$path/counter")
        if(counter.isNotEmpty()) {
            counter = counter.replace("\n", "")
            keysTable.counter = counter.toInt()
        } else
            keysTable.counter = 0

        return keysTable
    }

    fun save(path: String, keysTable : CKeysTable) {

        write("$path/userkey", keysTable.idHouse)
        write("$path/counter", "${keysTable.counter}")
    }
}


/***/
class CHouseEntryActivity : AppCompatActivity() {

    /** BACKEND */
    // -------------------------------------------------------------------

    // private var mViewModelDatabase: CDatabaseViewModel? = null

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
        // mViewModelDatabase = ViewModelProvider(this).get(CDatabaseViewModel::class.java)

        mTxtHouseEntryStatus = findViewById(R.id.txtHouseEntryStatus)
        mTxtHouseEntryDebugStatus = findViewById(R.id.txtHouseEntryDebugStatus)

        mLock = CDataBle("Замок", "", CBluetoothLeService.STATE_DISCONNECT)
        mBridge = CDataBle("Мост", "", CBluetoothLeService.STATE_DISCONNECT)
        mListBle = mutableListOf(mLock)

        // mHouse = intent.getSerializableExtra(resources.getString(R.string.intent_house)) as CKeysTable

        val gattServiceIntent = Intent(this, CBluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

        /** Activity result -> return ActivityResultLauncher<Intent> */
        mResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            finish()
        }

        mHouse = CFile.load(filesDir.path)
        if(mHouse.idHouse.isNotEmpty() && mHouse.counter != 0) {
            startActivity()
            mLoop = false
        }

        Thread { scanLoop() }.start()
    }

    fun startActivity() {
        val intentHouse = Intent(baseContext, CHouseCtrlActivity::class.java)
        intentHouse.putExtra(baseContext.getString(R.string.intent_house), mHouse)
        mResultLauncher.launch(intentHouse)
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
    }

    /***/
    override fun onPause() {
        super.onPause()

        /** Unregister receiver */
        unregisterReceiver(mGattUpdateReceiver)
    }

    /***/
    override fun onStop() {
        super.onStop()

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
                                val currentStatus = mHouse.status

                                intent.getByteArrayExtra("EXTRA_DATA")?.let { array ->
                                    dataString = array.toString(Charsets.UTF_8)

                                    if (mLock.address == current) {

                                        if (dataString[0] == '<' && dataString[1] == '!' || dataString[0] == '>') {

                                            if (mHouse.status < CStatus.COMMAND_LOCK_OPEN) {

                                                val pathFile = filesDir.absolutePath + File.separator + mHouse.idHouse


                                                pathFile.replace("\n", "")

                                                mControl.receivedData(array, pathFile)

                                                if (CSettings.checkFileInit(pathFile)) {

                                                    mHouse.status = CStatus.COMMAND_LOCK_OPEN

                                                    sendToOriginalThread { mTxtHouseEntryStatus.text = "Регистрация пользователя" }

                                                    mCommandsQueue.poll() // Не удаляется в общем цикле

                                                    addCommandLock(Operation.UNLOCK)
                                                    addCommandLock(Operation.LOCK)

                                                    CFile.save(filesDir.path, mHouse)
                                                }
                                            }
                                        }

                                        if (dataString.contains("<MNB>")) {

                                            if (mHouse.status < CStatus.COMMAND_GET_LOCK_KEY) {
                                                mHouse.status = CStatus.COMMAND_GET_LOCK_KEY

                                                sendToOriginalThread { mTxtHouseEntryStatus.text = "Получение ключа" }

                                                mCommandsQueue.poll() // Не успевает удалиться

                                                Thread.sleep(6000)

                                                addCommandLock(Operation.KEY)

                                                /*mControl.mCounter = 0
                                                mControl.mMacAddress = mHouse.idHouse.replace("-", "")

                                                val message = mControl.createOperation(Operation.KEY, mHouse.idHouse)
                                                mCommandsQueue.add(CCommand(mLock.address, message.asList()))*/
                                            }
                                        }

                                        if (dataString.contains("<CTY>") || dataString.contains("<CHG>")) {

                                            if (mHouse.status < CStatus.COMMAND_SAVE_USER) {
                                                mHouse.status = CStatus.COMMAND_SAVE_USER

                                                sendToOriginalThread { mTxtHouseEntryStatus.text = "Завершение регистрации" }

                                                mBluetoothService?.disconnect(mLock.address)

                                                startActivity()
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

                                                // mViewModelDatabase?.update(baseContext, mHouse)
                                            }
                                        }
                                    }
                                }

                                if (currentStatus != mHouse.status)
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

        if (!CBluetoothScanV.getInstance().permissions(this, packageManager)) {
            Toast.makeText(this, "Bluetooth not supported!", Toast.LENGTH_SHORT).show()
            finish()
        }

        CBluetoothScanV.getInstance().startScan(baseContext)

        while(mLoop) {

            /** Scan ble device */

            if (CBluetoothScanV.getInstance().getDiscovering()) {
                sendToOriginalThread { mTxtHouseEntryDebugStatus.append(".\n") }
                Thread.sleep(1000L)
                exitTimeout++
            }

            if (exitTimeout == 20) {
                sendToOriginalThread { Toast.makeText(baseContext, "Тайм-аут", Toast.LENGTH_LONG).show() }
                CBluetoothScanV.getInstance().stopScan()
                mLoop = false
                finish()
            }

            if (mLock.address.isEmpty()) {
                mLock.address = CBluetoothScanV.getInstance().getAddressLock()
                if (mLock.address.isNotEmpty()) {
                    sendToOriginalThread { mTxtHouseEntryDebugStatus.append("${mLock.name}: найден\n") }
                    mHouse.addressLock = mLock.address
                }
            }

            if (mBridge.address.isEmpty()) {
                mBridge.address = CBluetoothScanV.getInstance().getAddressBridge()
                if (mBridge.address.isNotEmpty()) {
                    sendToOriginalThread { mTxtHouseEntryDebugStatus.append("${mBridge.name}: найден\n") }
                    mHouse.addressBridge = mBridge.address

                    mConnectDevice = mBridge.address
                }
            }

            mBluetoothService?.let { bluetoothService ->

                // if (mLock.address.isNotEmpty() && mBridge.address.isNotEmpty()) {
                if (mLock.address.isNotEmpty()) {

                    if (CBluetoothScanV.getInstance().getDiscovering()) {

                        mConnectDevice = mLock.address

                        CBluetoothScanV.getInstance().stopScan()
                        sendToOriginalThread { mTxtHouseEntryStatus.text = "Ожидание команды" }
                    }

                    /** Connect ble device */
                    for (itemBle in mListBle)
                        if (itemBle.address == mConnectDevice && itemBle.state == CBluetoothLeService.STATE_DISCONNECT)
                            if(bluetoothService.connect(itemBle.address)) {
                                itemBle.state = CBluetoothLeService.STATE_CONNECTED

                                mHouse.counter = 0
                                mHouse.idHouse = mLock.address

                                mCommandsQueue.add(CCommand(mLock.address, listOf<Byte>('!'.code.toByte())))
                            }

                    /** Write ble device */
                    if (!mCommandsQueue.isEmpty()) {
                        val command = mCommandsQueue.element()
                        for ((name, address, state) in mListBle) {
                            if (address == command.address) {
                                if (state == CBluetoothLeService.STATE_CONNECT) {
                                    if (bluetoothService.writeCharacteristic(command.cmd.toByteArray(), address)) {

                                        // val dataString = command.cmd.toByteArray().toString(Charsets.UTF_8)
                                        // sendToOriginalThread { mTxtHouseCtrlConsole.append("${name}: сообщение отправлено: $dataString\n") }

                                        sendToOriginalThread { mTxtHouseEntryDebugStatus.append("${name}: сообщение отправлено\n") }

                                        if (mHouse.status != CStatus.COMMAND_GET_LOCK_KEY)
                                            mCommandsQueue.poll()

                                        if (mHouse.status == CStatus.COMMAND_GET_LOCK_KEY)
                                            Thread.sleep(2000L)

                                        if (mHouse.status == CStatus.COMMAND_LOCK_OPEN)
                                            Thread.sleep(2000L)

                                        if (mHouse.status == CStatus.COMMAND_SAVE_USER)
                                            Thread.sleep(4000L)
                                    }
                                }
                            }
                        }
                    } else {


                        if (mHouse.status == CStatus.COMMAND_SAVE_USER) {

                            addCommandBridge("registration_finish")
                        }

                        if (mHouse.status == CStatus.COMMAND_LOCK_OPEN) {

                            addCommandLock(Operation.UNLOCK)
                        }
                    }
                }
            }
        }
    }

    private fun addCommandBridge(message: String) {

        mConnectDevice = mBridge.address

        /*val date = CDataExchange()
        date.setCommand("phone_bridge_server")
        date.setStatus("success")
        date.setIdHouse(mHouse.idHouse)
        date.setCodePassword(mHouse.codePassword)
        date.setMessage(message)
        val arraySend = CClient.collectMessage(baseContext, date)
        mCommandsQueue.add(CCommand(mBridge.address, arraySend.toByteArray().asList()))*/
    }

    private fun addCommandLock(operation: Int) {

        try {

            val pathFile = filesDir.absolutePath + File.separator + mHouse.idHouse

            pathFile.replace("\n", "")

            mControl.mCounter = mHouse.counter++
            mControl.mMacAddress = mHouse.idHouse.replace(":", "")

            val message: ByteArray = mControl.createCommand(operation, pathFile)
            mCommandsQueue.add(CCommand(mLock.address, message.asList()))
        } catch (e: Exception) {
            print(e.toString())
        }
    }
}