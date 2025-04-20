package ru.rbkdev.rent.master.bluetooth

import android.os.Binder
import android.os.IBinder
import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

import java.util.*

/***/
class CBluetoothLeService : Service() {

    private var mBluetoothGatt: BluetoothGatt? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mExchangeCharacteristic: BluetoothGattCharacteristic? = null

    companion object {

        /***/
        const val BRIDGE_SERVICE_UUID: String = "91bad492-b950-4226-aa2b-4ede9fa42f59"

        /***/
        const val BRIDGE_CHARACTERISTIC_UUID: String = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

        /***/
        const val ACTION_DATA_READ: String = "ACTION_DATA_READ"

        /***/
        const val ACTION_DATA_WRITE: String = "ACTION_DATA_WRITE"

        /***/
        const val ACTION_DATA_AVAILABLE: String = "ACTION_DATA_AVAILABLE"

        /***/
        const val ACTION_GATT_CONNECTED: String = "ACTION_GATT_CONNECTED"

        /***/
        const val ACTION_GATT_DISCONNECTED: String = "ACTION_GATT_DISCONNECTED"

        /***/
        const val ACTION_GATT_SERVICES_DISCOVERED: String = "ACTION_GATT_SERVICES_DISCOVERED"

        /***/
        const val STATE_CONNECTED: Int = 0

        /***/
        const val STATE_CONNECT: Int = 3

        /***/
        const val STATE_DISCONNECT: Int = 4
    }

    /** Create service */
    // -------------------------------------------------------------------

    inner class LocalBinder : Binder() {
        /***/
        fun getService(): CBluetoothLeService {
            return this@CBluetoothLeService
        }
    }

    /***/
    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }

    /***/
    override fun onUnbind(intent: Intent?): Boolean {

        mBluetoothGatt?.let { gatt ->
            gatt.close()
            mBluetoothGatt = null
        }

        return super.onUnbind(intent)
    }

    // -------------------------------------------------------------------

    /** Get bluetooth adapter */
    fun initialize(context: Context): Boolean {

        val bluetoothManager = context.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        if (mBluetoothAdapter == null)
            return false
        return true
    }

    /** Connect gatt */
    @SuppressLint("HardwareIds")
    fun connect(address: String): Boolean {

        mBluetoothAdapter?.let { adapter ->

            if (adapter.address == address) {
                mBluetoothGatt?.let {
                    return it.connect()
                }
            }

            return try {
                val device = adapter.getRemoteDevice(address)
                mBluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                true
            } catch (exception: IllegalArgumentException) {
                false
            }
        }

        return false
    }

    /** Disconnect */
    fun disconnect() {
        mBluetoothGatt?.disconnect()
    }

    /** Broadcast update */
    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic?, gatt: BluetoothGatt?) {
        val intent = Intent(action)

        characteristic?.let { it ->

            intent.putExtra("EXTRA_DATA", it.value)
        }

        gatt?.let { bluetoothGatt ->
            bluetoothGatt.device?.let { bluetoothDevice ->
                intent.putExtra("EXTRA_ADDRESS", bluetoothDevice.address)
            }
        }

        sendBroadcast(intent)
    }

    /** Send data */
    fun writeCharacteristic(dataBytes: ByteArray): Boolean {

        var result = false

        mBluetoothAdapter?.let {

            mBluetoothGatt?.let { gatt ->

                mExchangeCharacteristic?.let { characteristic ->

                    characteristic.value = dataBytes

                    result = gatt.writeCharacteristic(characteristic)
                }
            }
        }

        return result
    }

    // -------------------------------------------------------------------

    /** Bluetooth gatt callback */
    private val bluetoothGattCallback = object : BluetoothGattCallback() {

        /** Link */
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {
                    broadcastUpdate(ACTION_GATT_CONNECTED, null, gatt)
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    broadcastUpdate(ACTION_GATT_DISCONNECTED, null, gatt)
                }
            }
        }

        /** Find service */
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {

            if (status == BluetoothGatt.GATT_SUCCESS) {

                gatt?.let { bluetoothGatt ->

                    findGattServices(bluetoothGatt.services)
                }

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null, gatt)
            }
        }

        private fun findGattServices(gattServices: List<BluetoothGattService>?) {

            if (gattServices == null) {
                return
            }
            mExchangeCharacteristic = null
            for (gattService in gattServices) {

                val serviceUUID = gattService.uuid.toString()

                if (BRIDGE_SERVICE_UUID.contains(serviceUUID)) {

                    val gattCharacteristics = gattService.characteristics
                    for (gattCharacteristic in gattCharacteristics) {

                        val characteristicProperties = gattCharacteristic.properties
                        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0)
                            setCharacteristicNotification(gattCharacteristic, true)

                        if (BRIDGE_CHARACTERISTIC_UUID.contains(gattCharacteristic.uuid.toString())) {
                            mExchangeCharacteristic = gattCharacteristic
                            break
                        }
                    }
                    break
                }
            }
        }

        fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enabled: Boolean) {

            if (mBluetoothAdapter == null || mBluetoothGatt == null)
                return

            mBluetoothGatt?.setCharacteristicNotification(characteristic, enabled)
            val descriptors = characteristic.descriptors
            for (i in descriptors.indices) {
                val descriptor = descriptors[i]
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                mBluetoothGatt?.writeDescriptor(descriptor)
            }
        }

        /** Send data */
        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {

            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_DATA_WRITE, characteristic, gatt)
        }

        /** receive data */
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {

            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_DATA_READ, characteristic, gatt)
        }

        /** Changed data */
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, gatt)
        }
    }
}