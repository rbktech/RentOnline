package ru.rbkdev.rent.master

import ru.rbkdev.rent.master.ins.sendToOriginalThread
import ru.rbkdev.rent.master.bluetooth.CBluetoothScanV
import ru.rbkdev.rent.master.bluetooth.CBluetoothLeService

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.JsonWriter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AppCompatActivity

import java.io.OutputStreamWriter
import java.io.ByteArrayOutputStream
import java.util.*

class CMainActivity : AppCompatActivity() {

    private var mState: Int = CBluetoothLeService.STATE_DISCONNECT
    private var mLoop: Boolean = true

    /** Bluetooth service */
    private var mBluetoothService: CBluetoothLeService? = null

    /** Queue commands */
    private val mCommandsQueue: Queue<List<Byte>> = ArrayDeque()

    private lateinit var mTxtMainIpAddress: EditText
    private lateinit var mTxtMainPort: EditText
    private lateinit var mTxtMainWiFiName: EditText
    private lateinit var mTxtMainWiFiPassword: EditText
    private lateinit var mTxtMainAddressLocal: EditText
    private lateinit var mBtnMainSave: Button

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        mTxtMainIpAddress = findViewById(R.id.txtMainIpAddress)
        mTxtMainPort = findViewById(R.id.txtMainPort)
        mTxtMainWiFiName = findViewById(R.id.txtMainWiFiName)
        mTxtMainWiFiPassword = findViewById(R.id.txtMainWiFiPassword)
        mTxtMainAddressLocal = findViewById(R.id.txtMainAddressLocal)
        mBtnMainSave = findViewById(R.id.btnMainSave)

        mBtnMainSave.setOnClickListener {
            mCommandsQueue.add(collectMessage(baseContext).toByteArray().asList())
        }

        val gattServiceIntent = Intent(this, CBluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

        Thread { scanLoop() }.start()
    }

    /** Interface service */
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {

            mBluetoothService = (service as CBluetoothLeService.LocalBinder).getService()
            mBluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize(baseContext))
                    finish()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothService = null
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
    }

    /***/
    override fun onPause() {
        super.onPause()

        /** Unregister receiver */
        unregisterReceiver(mGattUpdateReceiver)
    }

    override fun onStop() {
        super.onStop()
        mBluetoothService?.disconnect()
        mLoop = false
    }

    /** Callback service */
    private val mGattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {

                CBluetoothLeService.ACTION_DATA_AVAILABLE -> {

                    intent.getByteArrayExtra("EXTRA_DATA")?.let { array ->
                        print(array.toString(Charsets.UTF_8))
                    }
                }

                CBluetoothLeService.ACTION_GATT_CONNECTED -> {
                    mState = CBluetoothLeService.STATE_CONNECTED
                }

                CBluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    mState = CBluetoothLeService.STATE_DISCONNECT
                }

                CBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    mState = CBluetoothLeService.STATE_CONNECT
                }
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scanLoop() {

        if (!CBluetoothScanV.getInstance().permissions(this, packageManager)) {
            Toast.makeText(this, "Bluetooth not supported!", Toast.LENGTH_SHORT).show()
            finish()
        }

        CBluetoothScanV.getInstance().startScan(baseContext, resources.getString(R.string.app_name))

        while (mLoop) {

            if (!CBluetoothScanV.getInstance().getDiscovering()) {

                if (mCommandsQueue.isNotEmpty()) {
                    val command = mCommandsQueue.element()

                    mBluetoothService?.also { bluetoothService ->

                        when (mState) {

                            CBluetoothLeService.STATE_CONNECT -> {
                                if (bluetoothService.writeCharacteristic(command.toByteArray())) {
                                    mCommandsQueue.poll()
                                }
                            }

                            CBluetoothLeService.STATE_DISCONNECT -> {
                                if (bluetoothService.connect(CBluetoothScanV.getInstance().getAddressBridge()))
                                    mState = CBluetoothLeService.STATE_CONNECTED
                            }
                        }
                    }
                }

            } else {
                if (CBluetoothScanV.getInstance().isStopScan()) {
                    sendToOriginalThread { mBtnMainSave.isEnabled = true }
                    CBluetoothScanV.getInstance().stopScan()
                }
            }
        }
    }

    /***/
    private fun collectMessage(context: Context): ByteArrayOutputStream {

        val listIpAddress = mTxtMainIpAddress.text.toString().split('.')

        val outStream = ByteArrayOutputStream()
        val jsonWriter = JsonWriter(OutputStreamWriter(outStream, "UTF-8"))

        jsonWriter.beginArray()

        jsonWriter.beginObject()

        jsonWriter.name(context.getString(R.string.json_command)).value(context.getString(R.string.json_command_settings))
        jsonWriter.name(context.getString(R.string.json_status)).value(context.getString(R.string.json_success))
        jsonWriter.name(context.getString(R.string.json_ip_address_0)).value(listIpAddress[0])
        jsonWriter.name(context.getString(R.string.json_ip_address_1)).value(listIpAddress[1])
        jsonWriter.name(context.getString(R.string.json_ip_address_2)).value(listIpAddress[2])
        jsonWriter.name(context.getString(R.string.json_ip_address_3)).value(listIpAddress[3])
        jsonWriter.name(context.getString(R.string.json_port)).value(mTxtMainPort.text.toString())
        jsonWriter.name(context.getString(R.string.json_wifi_name)).value(mTxtMainWiFiName.text.toString())
        jsonWriter.name(context.getString(R.string.json_wifi_password)).value(mTxtMainWiFiPassword.text.toString())
        jsonWriter.name(context.getString(R.string.json_address_local)).value(mTxtMainAddressLocal.text.toString())

        jsonWriter.endObject()

        jsonWriter.endArray()

        jsonWriter.close()

        return outStream
    }
}