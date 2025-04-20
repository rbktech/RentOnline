package ru.rbkdev.rent.master.bluetooth

import ru.rbkdev.rent.master.R

import android.content.Context
import android.bluetooth.BluetoothAdapter

/***/
class CBluetoothScan {

    /** Bluetooth adapter */
    private var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var mAddressBridge: String = ""

    /** Flag for ble scan */
    private var mDiscovering = false

    /** Static */
    companion object {

        private var mInstance: CBluetoothScan? = null

        /** Singleton */
        fun getInstance(): CBluetoothScan {

            return mInstance ?: synchronized(this) {

                CBluetoothScan().let {

                    mInstance = it

                    it
                }
            }
        }
    }

    /***/
    fun getAddressBridge(): String = mAddressBridge

    /***/
    fun getDiscovering(): Boolean = mDiscovering

    /***/
    fun startScan(context: Context) {

        mDiscovering = true
        mBluetoothAdapter.startLeScan { device, _, _ ->

            device.name?.let { name ->

                if (name == context.getString(R.string.app_name))
                    mAddressBridge = device.address
            }
        }
    }

    /***/
    fun stopScan() {

        mDiscovering = false
        mBluetoothAdapter.stopLeScan { _, _, _ -> }
    }
}