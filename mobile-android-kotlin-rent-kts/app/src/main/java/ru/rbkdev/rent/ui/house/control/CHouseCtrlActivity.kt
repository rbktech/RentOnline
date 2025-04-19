package ru.rbkdev.rent.ui.house.control

import ru.rbkdev.rent.R

import ru.rbkdev.rent.bluetooth.CBluetoothLeService
import ru.rbkdev.rent.room.database.keys.CKeysTable
import ru.rbkdev.rent.tools.sendToOriginalThread
import ru.rbkdev.rent.ui.house.entry.CFile

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

import java.io.File

import java.util.*

object Operation {
    val UNLOCK: Int = 0
    val LOCK: Int = 1
    val KEY: Int = 2
}

class CControl {
    var mCounter: Int = 0
    var mMacAddress: String = ""

    fun createCommand(operation: Int, path: String) : ByteArray
    {
        return byteArrayOf()
    }

    fun receivedData(array: ByteArray, path: String)
    {

    }
}

/***/
class CHouseCtrlActivity : AppCompatActivity() {

    private var mLoop: Boolean = true
    private var mState: Int = CBluetoothLeService.STATE_DISCONNECT
    private val mCommandsQueue: Queue<List<Byte>> = ArrayDeque()
    private lateinit var mHouse: CKeysTable

    private val mControl: CControl = CControl()

    /** Bluetooth service */
    private var mBluetoothService: CBluetoothLeService? = null

    private lateinit var mBtnHouseCtrlOpen: Button
    private lateinit var mBtnHouseCtrlClose: Button
    private lateinit var btnHouseCtrlClear: Button
    private lateinit var mTxtHouseCtrlStatus: TextView
    private lateinit var txtHouseCtrlCount: TextView

    /** BACKEND */
    // -------------------------------------------------------------------

    // private var mViewModelDatabase: CDatabaseViewModel? = null

    /***/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.house_ctrl_activity)

        mHouse = intent.getSerializableExtra(resources.getString(R.string.intent_house)) as CKeysTable

        /** Settings view module database */
        // mViewModelDatabase = ViewModelProvider(this).get(CDatabaseViewModel::class.java)

        mBtnHouseCtrlOpen = findViewById(R.id.btnHouseCtrlOpen)
        mBtnHouseCtrlClose = findViewById(R.id.btnHouseCtrlClose)
        mTxtHouseCtrlStatus = findViewById(R.id.txtHouseCtrlStatus)
        btnHouseCtrlClear = findViewById(R.id.btnHouseCtrlClear)
        txtHouseCtrlCount = findViewById(R.id.txtHouseCtrlCount)

        mBtnHouseCtrlOpen.setOnClickListener {

            addCommandLock(Operation.UNLOCK)
            // enableInterface(false)
        }

        btnHouseCtrlClear.isEnabled = false

        btnHouseCtrlClear.setOnClickListener {

            mHouse.counter = 0
            mHouse.idHouse = ""

            CFile.save(filesDir.path, mHouse)
        }

        mBtnHouseCtrlClose.setOnClickListener {

            addCommandLock(Operation.LOCK)
            // enableInterface(false)
        }

        val gattServiceIntent = Intent(this, CBluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

        Thread { loop() }.start()
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

        CFile.save(filesDir.path, mHouse)

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

                when (intent.action) {

                    CBluetoothLeService.ACTION_DATA_AVAILABLE -> {
                        intent.getByteArrayExtra("EXTRA_DATA")?.let { array ->

                            var dataRecv = array.toString(Charsets.UTF_8)

                            if (dataRecv.contains('<') && dataRecv.contains('>')) {

                                enableInterface(true)

                                mCommandsQueue.poll()
                            }

                            // dataRecv.prependIndent("Статус: сообщение пришло: ")
                            dataRecv = "Статус: сообщение пришло"

                            sendToOriginalThread { mTxtHouseCtrlStatus.text = dataRecv }
                        }
                    }

                    CBluetoothLeService.ACTION_GATT_CONNECTED -> {
                        mState = CBluetoothLeService.STATE_CONNECTED
                        sendToOriginalThread { mTxtHouseCtrlStatus.text = "Статус: соединение" }
                    }

                    CBluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                        mState = CBluetoothLeService.STATE_DISCONNECT
                        sendToOriginalThread { mTxtHouseCtrlStatus.text = "Статус: рассоединение" }
                    }

                    CBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                        mState = CBluetoothLeService.STATE_CONNECT
                        sendToOriginalThread { mTxtHouseCtrlStatus.text = "Статус: открыт" }
                    }
                }
            }
        }
    }

    private fun loop() {

        mHouse.idHouse =         mHouse.idHouse.replace("\n", "")

        while (mLoop) {

            txtHouseCtrlCount.text = "${mCommandsQueue.size}"

            if (!mCommandsQueue.isEmpty()) {

                val command = mCommandsQueue.element()
                mBluetoothService?.also { bluetoothService ->
                    mHouse.let { house ->

                        when (mState) {

                            CBluetoothLeService.STATE_CONNECT -> {
                                if (bluetoothService.writeCharacteristic(command.toByteArray(), house.idHouse)) {
                                    sendToOriginalThread { mTxtHouseCtrlStatus.text = "Статус: сообщение отправлено" }

                                    Thread.sleep(3000L)
                                }
                            }

                            CBluetoothLeService.STATE_DISCONNECT -> {
                                if (bluetoothService.connect(house.idHouse)) {
                                    mState = CBluetoothLeService.STATE_CONNECTED

                                    Thread.sleep(3000L)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun enableInterface(enable: Boolean) {

        sendToOriginalThread {
            mBtnHouseCtrlOpen.isEnabled = enable
            mBtnHouseCtrlClose.isEnabled = enable
        }
    }

    private fun addCommandLock(operation: Int) {

        mHouse.let { house ->

            var pathFile = filesDir.absolutePath + File.separator + house.idHouse

            try {

                pathFile = pathFile.replace("\n", "")

                mControl.mCounter = house.counter++
                mControl.mMacAddress = house.idHouse.replace(":", "")
                mControl.mMacAddress = mControl.mMacAddress.replace("\n", "")

                CFile.save(filesDir.path, house)

                val message: ByteArray = mControl.createCommand(operation, pathFile)
                mCommandsQueue.add(message.asList())

                // mViewModelDatabase?.update(baseContext, house)

            } catch (e: Exception) {
                print(e.toString())
            }
        }
    }
}