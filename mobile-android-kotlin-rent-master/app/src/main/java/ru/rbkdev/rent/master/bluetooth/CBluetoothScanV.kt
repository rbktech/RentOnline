package ru.rbkdev.rent.master.bluetooth

import android.os.Build
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.BluetoothLeScanner

import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CBluetoothScanV {

    private var mBluetoothLeScanner: BluetoothLeScanner? = null

    private var mAddressBridge: String = ""

    /** Flag for ble scan */
    private var mDiscovering = false

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    /** Static */
    companion object {

        private var mInstance: CBluetoothScanV? = null

        /** Singleton */
        fun getInstance(): CBluetoothScanV {

            return mInstance ?: synchronized(this) {

                CBluetoothScanV().let {

                    mInstance = it

                    it
                }
            }
        }
    }

    fun permissions(activity: AppCompatActivity, packageManager: PackageManager): Boolean {

        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH) }?.also {

            print("bluetooth_not_supported")
            return false
        }

        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {

            print("ble_not_supported")
            return false
        }

        ActivityCompat.requestPermissions(
            activity, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                // Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), 0
        )

        return true
    }

    /***/
    fun getAddressBridge(): String = mAddressBridge

    fun isStopScan(): Boolean = mAddressBridge.isNotEmpty()

    /***/
    fun getDiscovering(): Boolean = mDiscovering

    /***/
    fun startScan(context: Context, nameScan: String) {

        val filters = ArrayList<ScanFilter?>()
        filters.add(ScanFilter.Builder().setDeviceName(nameScan).build())

        val bluetoothManager = context.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothLeScanner = bluetoothManager.adapter?.bluetoothLeScanner

        mBluetoothLeScanner?.startScan(filters, ScanSettings.Builder().build(), mLeScanCallback)

        mDiscovering = true
    }

    /***/
    fun stopScan() {

        mBluetoothLeScanner?.stopScan(mLeScanCallback)

        mDiscovering = false
    }

    private val mLeScanCallback: ScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {

            mAddressBridge = result.device.address
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            super.onBatchScanResults(results)
            print("")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            print("")
        }
    }
}
