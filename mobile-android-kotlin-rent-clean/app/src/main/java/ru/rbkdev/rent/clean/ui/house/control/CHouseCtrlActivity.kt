package ru.rbkdev.rent.clean.ui.house.control

import ru.rbkdev.rent.clean.R

import ru.rbkdev.rent.clean.bluetooth.CBluetoothLeService
import ru.rbkdev.rent.clean.tools.sendToOriginalThread

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity

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
    private var mAddress: String = ""

    private val mControl: CControl = CControl()

    /** Bluetooth service */
    private var mBluetoothService: CBluetoothLeService? = null

    private lateinit var mTxtHouseCtrlStatus: TextView

    /***/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.house_ctrl_activity)

        intent.getStringExtra(resources.getString(R.string.intent_address))?.let { address ->
            mAddress = address
        }

        val btnHouseCtrlOpen = findViewById<Button>(R.id.btnHouseCtrlOpen)
        val btnHouseCtrlClose = findViewById<Button>(R.id.btnHouseCtrlClose)
        mTxtHouseCtrlStatus = findViewById(R.id.txtHouseCtrlStatus)

        btnHouseCtrlOpen.setOnClickListener {

            try {
                val message: ByteArray = mControl.createCommand(Operation.UNLOCK, "")
                mCommandsQueue.add(message.toList())
            } catch (e: Exception) {
                print(e.toString())
            }
        }

        btnHouseCtrlClose.setOnClickListener {

            try {
                val message: ByteArray = mControl.createCommand(Operation.LOCK, "")
                mCommandsQueue.add(message.toList())
            } catch (e: Exception) {
                print(e.toString())
            }
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

                when (intent.action) {

                    CBluetoothLeService.ACTION_DATA_READ -> {

                        // sendToOriginalThread { mTxtHouseCtrlStatus.text = "Статус: чтение" }
                    }

                    CBluetoothLeService.ACTION_DATA_WRITE -> {

                        // sendToOriginalThread { mTxtHouseCtrlStatus.text = "Статус: запись" }
                    }

                    CBluetoothLeService.ACTION_DATA_AVAILABLE -> {
                        intent.getByteArrayExtra("EXTRA_DATA")?.let { array ->

                            val dataRecv = "Статус: сообщение пришло: ${array.toString(Charsets.UTF_8)}"
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

        while (mLoop) {

            if (!mCommandsQueue.isEmpty()) {

                val command = mCommandsQueue.element()
                mBluetoothService?.also { bluetoothService ->

                    when (mState) {

                        CBluetoothLeService.STATE_CONNECT -> {
                            if (bluetoothService.writeCharacteristic(command.toByteArray(), mAddress)) {
                                sendToOriginalThread { mTxtHouseCtrlStatus.text = "Статус: сообщение отправлено" }
                                mCommandsQueue.poll()
                            }
                        }

                        CBluetoothLeService.STATE_DISCONNECT -> {
                            if (bluetoothService.connect(mAddress))
                                mState = CBluetoothLeService.STATE_CONNECTED
                        }
                    }
                }
            }
        }

        Thread.sleep(1000L)
    }
}