package ru.rbkdev.rent.bluetooth

import android.util.Log
import android.os.Binder
import android.os.IBinder
import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.annotation.SuppressLint

object BleSettings {

    val BLE_SERVICES = arrayOf(
        "",
        "",
        ""
    )
    val BLE_WRITE_CHARACTERISTICS = arrayOf(
        "",
        "",
        ""
    )
    val BLE_READ_CHARACTERISTICS = arrayOf(
        "",
        "",
        ""
    )
}

/***/
data class TBluetoothGatt(

    /***/
    var macAddress: String = "",

    /***/
    var mBluetoothGatt: BluetoothGatt? = null,

    /***/
    var mExchangeCharacteristic: BluetoothGattCharacteristic? = null
)

/***/
class CBluetoothLeService : Service() {

    private val mListBluetoothGatt = mutableListOf<TBluetoothGatt>()

    // private var mListBluetoothGatt = mutableMapOf<String, BluetoothGatt?>()
    // private var mExchangeCharacteristic: BluetoothGattCharacteristic? = null

    // private var mBluetoothGatt: BluetoothGatt? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null

    // private var mConnectionState = STATE_DISCONNECTED

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
        // const val STATE_DISCOVERED: Int = 1

        /***/
        // const val STATE_DISCONNECTED: Int = 2

        /***/
        const val STATE_CONNECT: Int = 3

        /***/
        const val STATE_DISCONNECT: Int = 4

        /***/
        // const val STATE_DATA_EXCHANGE_START: Int = 5

        /***/
        // const val STATE_DATA_EXCHANGE_FINISH: Int = 6
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

        for (item in mListBluetoothGatt) {

            item.mBluetoothGatt?.let { gatt ->
                gatt.close()
                item.mBluetoothGatt = null
            }

            item.mExchangeCharacteristic = null
        }

        /*mBluetoothGatt?.let { gatt ->
            gatt.close()
            mBluetoothGatt = null
        }*/

        return super.onUnbind(intent)
    }

    // -------------------------------------------------------------------

    /** Get bluetooth adapter */
    fun initialize(): Boolean {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null)
            return false
        return true
    }

    /** Connect gatt */
    @SuppressLint("HardwareIds")
    fun connect(address: String): Boolean {

        mBluetoothAdapter?.let { adapter ->

            for ((macAddress, bluetoothGatt) in mListBluetoothGatt) {
                if (macAddress == address) {
                    bluetoothGatt?.let { gatt ->
                        return gatt.connect()
                    }
                }
            }

            return try {
                val device = adapter.getRemoteDevice(address)
                val bluetoothGatt = TBluetoothGatt()
                bluetoothGatt.macAddress = address
                bluetoothGatt.mBluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                mListBluetoothGatt.add(bluetoothGatt)
                true
            } catch (exception: IllegalArgumentException) {
                false
            }

            /*if (adapter.address == address) {
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

             */
        }

        return false
    }

    /***/
    fun disconnect(address: String) {

        for ((macAddress, mBluetoothGatt) in mListBluetoothGatt)
            if (macAddress == address)
                mBluetoothGatt?.disconnect()
    }

    /***/
/*fun checkConnected(): Boolean {

    /*return when (mConnectionState) {
        STATE_DISCOVERED -> true
        else -> false
    }*/
    return false
}*/

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

    /***/
    private fun toByteArray(list: List<Byte>): ByteArray {
        val n = list.size
        val ret = ByteArray(n)
        for (i in 0 until n) {
            ret[i] = list[i]
        }
        return ret
    }


    /** Send data */
    fun writeCharacteristic(dataBytes: ByteArray, address: String): Boolean {

        var result = false

        mBluetoothAdapter?.let {

            for ((macAddress, mBluetoothGatt, mExchangeCharacteristic) in mListBluetoothGatt) {
                if (macAddress == address) {
                    mBluetoothGatt?.let { bluetoothGatt ->
                        mExchangeCharacteristic?.let { characteristic ->

                            /*val bytes = ArrayList<Byte>()

                        for (i in dataBytes.indices) {
                            bytes.add(dataBytes[i])
                            if (bytes.size == 20 || i == dataBytes.size - 1) {
                                characteristic.setValue(toByteArray(bytes))
                                gatt.writeCharacteristic(characteristic)
                                bytes.clear()
                                if (i == dataBytes.size - 1) {
                                    //isSending = false
                                    Log.v("GGGG", "Sending Finished")
                                    Thread.sleep(250)
                                } else {
                                    Thread.sleep(250)
                                }
                            }
                        }*/

                            characteristic.value = dataBytes

                            result = bluetoothGatt.writeCharacteristic(characteristic)
                        }
                    }
                }
            }
        }

        Log.v("GGGG", "send result: $result")
        Log.v("GGGG", "send data: $dataBytes")

        return result

        /*if(mBluetoothAdapter == null || mBluetoothGatt == null)
            return

        while(mExchangeCharacteristic == null) {
            return
            //Toast.makeText(applicationContext, "mExchangeCharacteristic==null", Toast.LENGTH_SHORT).show()
        }

        // Toast.makeText(applicationContext, "mExchangeCharacteristic!=null", Toast.LENGTH_SHORT).show()

        mExchangeCharacteristic?.value = byteArray
        val result = mBluetoothGatt?.writeCharacteristic(mExchangeCharacteristic)
        print("")*/
    }

// -------------------------------------------------------------------

    /** Bluetooth gatt callback */
    private val bluetoothGattCallback = object : BluetoothGattCallback() {

        /** Link */
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {
                    // mConnectionState = STATE_CONNECTED
                    broadcastUpdate(ACTION_GATT_CONNECTED, null, gatt)
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    // mConnectionState = STATE_DISCONNECTED
                    broadcastUpdate(ACTION_GATT_DISCONNECTED, null, gatt)
                }
            }
        }

        /** Find service */
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {

            if (status == BluetoothGatt.GATT_SUCCESS) {

                gatt?.let { bluetoothGatt ->

                    findGattServices(bluetoothGatt.services, bluetoothGatt.device.address)

                    /*for (service in bluetoothGatt.services) {
                        for (characteristic in service.characteristics) {

                            //bluetoothGatt.setCharacteristicNotification(characteristic, true)


                            // Test each characteristic in the list of characteristics
                            val charUUID: String = characteristic.uuid.toString()

                            if (BRIDGE_CHARACTERISTIC_UUID.contains(service.uuid.toString()))
                                print("")

                            if (BRIDGE_CHARACTERISTIC_UUID.contains(charUUID)) {
                                mExchangeCharacteristic = characteristic
                            }

                            //if (uuid.equals(BleSettings.MLDP_DATA_PRIVATE_CHAR))
                            //if (uuid.equals(BleSettings.MLDP_DATA_PRIVATE_CHAR))
                            if (Arrays.asList<Any>(BleSettings.BLE_WRITE_CHARACTERISTICS).contains(charUUID)) {
                                // mService = gattService
                                //mWriteCharacteristic = gattCharacteristic //If so then save the reference to the characteristic
                                // Log("Found MLDP data characteristics")
                            }

                            if (Arrays.asList<Any>(BleSettings.BLE_READ_CHARACTERISTICS).contains(charUUID)) {
                                //mService = gattService
                                //mReadCharacteristic = gattCharacteristic // If so then save the reference to the characteristic
                                //Log("Found MLDP control characteristics")
                            }

                            val characteristicProperties: Int = characteristic.properties //Get the properties of the characteristic

                            if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) { //See if the characteristic has the Notify property
                                //mBluetoothLeService.setCharacteristicNotification(characteristic, true) //If so then enable notification in the BluetoothGatt
                            }

                            if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) { //See if the characteristic has the Indicate property
                                //mBluetoothLeService.setCharacteristicIndication(characteristic, true) //If so then enable notification (and indication) in the BluetoothGatt
                            }

                            if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) { //See if the characteristic has the Write (acknowledged) property
                                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            }

                            if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                            }

                            if (listOf<Any>(*BleSettings.BLE_WRITE_CHARACTERISTICS).contains(charUUID)) {   // See if it matches the UUID of the MLDP data characteristic
                                // mService = service
                                mExchangeCharacteristic = characteristic //If so then save the reference to the characteristic
                            }

                            /*if (listOf<Any>(*BleSettings.BLE_READ_CHARACTERISTICS).contains(charUUID)) {   // See if UUID matches the UUID of the MLDP control characteristic
                                // mService = service
                                // mReadCharacteristic = characteristic // If so then save the reference to the characteristic
                            }*/

                            //val characteristicProperties: Int = characteristic.properties //Get the properties of the characteristic

                            /*if(characteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) { //See if the characteristic has the Notify property
                                // mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true) //If so then enable notification in the BluetoothGatt
                            }*/

                            if ((characteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) { //See if the characteristic has the Notify property
                                bluetoothGatt.setCharacteristicNotification(characteristic, true)

                                for (descriptor in characteristic.descriptors) {
                                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                    bluetoothGatt.writeDescriptor(descriptor)
                                }

                                /*val descriptors = characteristic.descriptors
                                for (i in descriptors.indices) {
                                    val descriptor = descriptors[i] //Get the descripter that enables notification on the server
                                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE //Set the value of the descriptor to enable notification
                                    bluetoothGatt.writeDescriptor(descriptor) //Write the descriptor
                                }*/
                            }

                            /*if((characteristicProperties and BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) { //See if the characteristic has the Write (acknowledged) property
                                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT //If so then set the write type (write with acknowledge) in the BluetoothGatt
                            }

                            if((characteristicProperties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) { //See if the characteristic has the Write (unacknowledged) property
                                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE //If so then set the write type (write with no acknowledge) in the BluetoothGatt
                            }*/

                            //if(characteristic.properties and 0xF0 == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {}
                        }
                    }*/
                }

                // mConnectionState = STATE_DISCOVERED

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null, gatt)
            }
        }

        private fun findGattServices(gattServices: List<BluetoothGattService>?, address: String) {

            if (gattServices == null) {
                return
            }
            // mExchangeCharacteristic = null
            for (gattService in gattServices) {

                val serviceUUID = gattService.uuid.toString()

                if (BRIDGE_SERVICE_UUID.contains(serviceUUID)) {

                    val gattCharacteristics = gattService.characteristics
                    for (gattCharacteristic in gattCharacteristics) {

                        val characteristicProperties = gattCharacteristic.properties
                        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0)
                            setCharacteristicNotification(gattCharacteristic, true, address)

                        if (BRIDGE_CHARACTERISTIC_UUID.contains(gattCharacteristic.uuid.toString())) {

                            for (item in mListBluetoothGatt)
                                if (item.macAddress == address)
                                    item.mExchangeCharacteristic = gattCharacteristic


                            //mExchangeCharacteristic = gattCharacteristic
                            break
                        }
                    }
                    break
                }

                if (listOf<Any>(*BleSettings.BLE_SERVICES).contains(serviceUUID)) {

                    val gattCharacteristics = gattService.characteristics
                    for (gattCharacteristic in gattCharacteristics) {

                        val charUUID = gattCharacteristic.uuid.toString()

                        if (listOf<Any>(*BleSettings.BLE_WRITE_CHARACTERISTICS).contains(charUUID)) {
                            // if (Arrays.asList<Any>(BleSettings.BLE_WRITE_CHARACTERISTICS).contains(charUUID)) {

                            for (item in mListBluetoothGatt)
                                if (item.macAddress == address)
                                    item.mExchangeCharacteristic = gattCharacteristic

                            // mExchangeCharacteristic = gattCharacteristic
                        }

                        val characteristicProperties = gattCharacteristic.properties
                        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                            setCharacteristicNotification(gattCharacteristic, true, address)
                        }

                        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                            gattCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        }

                        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                            gattCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                        }
                    }
                    // break
                }
            }
        }

        fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enabled: Boolean, address: String) {

            for ((macAddress, mBluetoothGatt) in mListBluetoothGatt) {
                if (macAddress == address) {

                    if (mBluetoothAdapter == null || mBluetoothGatt == null)
                        return

                    mBluetoothGatt.setCharacteristicNotification(characteristic, enabled)
                    val descriptors = characteristic.descriptors
                    for (i in descriptors.indices) {
                        val descriptor = descriptors[i]
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        mBluetoothGatt.writeDescriptor(descriptor)
                    }
                }
            }

            /*if (mBluetoothAdapter == null || mBluetoothGatt == null)
                return

            mBluetoothGatt?.setCharacteristicNotification(characteristic, enabled)
            val descriptors = characteristic.descriptors
            for (i in descriptors.indices) {
                val descriptor = descriptors[i]
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                mBluetoothGatt?.writeDescriptor(descriptor)
            }*/
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