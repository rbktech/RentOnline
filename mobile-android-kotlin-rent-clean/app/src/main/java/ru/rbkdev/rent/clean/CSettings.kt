package ru.rbkdev.rent.clean

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

    fun setLinkPreference(context: Context, ip: String, port: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("ip", ip).apply()
        // PreferenceManager.getDefaultSharedPreferences(context).edit().putString("port", port).apply()
    }

    fun setLoginPreference(context: Context, login: String, password: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("login", login).apply()
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("password", password).apply()
    }

    /***/
    fun getLogin(context: Context): String {
        return getPreference(context, "login", R.string.default_null)
    }

    /***/
    fun getPassword(context: Context): String {
        return getPreference(context, "password", R.string.default_null)
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
        fun checkFileInit(key: String, certificate: String): Boolean {

            var result = 0
            val files = File("./data/data/ru.rbkdev.rent.clean/files").listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        if (file.name == key)
                            result++
                        if (file.name == "$certificate.ser")
                            result++
                    }
                }
            }
            return result == 2
        }

        /***/
        fun deleteFileInit() {
            val files = File("./data/data/ru.rbkdev.rent.clean/files").listFiles()
            if (files != null)
                for (file in files)
                    file.delete()
        }
    }
}