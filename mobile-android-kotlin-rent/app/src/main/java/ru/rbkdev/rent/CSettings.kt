package ru.rbkdev.rent

import android.content.Context
import android.preference.PreferenceManager

import java.io.*

/***/
class CSettings {

    private var mUserId: String = ""

    /***/
    fun getUserId(): String {
        return mUserId
    }

    /***/
    fun setUserId(userId: String) {
        mUserId = userId
    }

    /***/
    fun getIp(context: Context): String {
        return getPreference(context, "ip", R.string.default_ip)
    }

    /***/
    fun getPortInt(context: Context): Int {
        return getPreference(context, "port", R.string.default_port).toInt()
    }

    /***/
    fun getTimeout(context: Context): Int {
        return getPreference(context, "timeout", R.string.default_timeout).toInt()
    }

    /***/
    fun getAddressLock(context: Context): String {
        return getPreference(context, "address_lock", R.string.default_address)
    }

    /***/
    fun getAddressUser(context: Context): String {
        return getPreference(context, "address_user", R.string.default_address)
    }

    /***/
    fun getAddressBridge(context: Context): String {
        return getPreference(context, "address_bridge", R.string.default_address)
    }

    private fun getPreference(context: Context, key: String, defaultKey: Int): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (preferences != null) {

            val preference = preferences.getString(key, context.getString(defaultKey))
            if (preference != null)
                return preference
        }

        return ""
    }

    /***/
    fun setAddressLock(context: Context, address: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("address_lock", address).apply()
    }

    /***/
    fun setAddressUser(context: Context, address: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("address_user", address).apply()
    }

    /***/
    fun setAddressBridge(context: Context, address: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("address_bridge", address).apply()
    }

    /** Static */
    companion object {

        private var mInstance: CSettings? = null

        /** Singleton */
        fun getInstance(): CSettings {

            return mInstance ?: synchronized(this) {

                CSettings().let {

                    mInstance = it

                    it
                }
            }
        }

        /***/
        fun checkFileInit(pathFile: String): Boolean {

            var result = 0
            val files = File(pathFile).listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        if (file.name == "key")
                            result++
                        if (file.name == "certificate")
                            result++
                    }
                }
            }
            return result == 2
        }
    }
}