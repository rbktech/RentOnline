package ru.rbkdev.rent.master

import android.content.Context
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity

class CLocation {

    fun statusCheck(context: Context) {
        val manager = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager?
        manager?.let { it ->
            if (!it.isProviderEnabled(LocationManager.GPS_PROVIDER))
                buildAlertMessageNoGps(context)
        }
    }

    private fun buildAlertMessageNoGps(context: Context) {
        /*val builder: AlertDialog.Builder = Builder(context)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert: AlertDialog = builder.create()
        alert.show()*/
    }
}