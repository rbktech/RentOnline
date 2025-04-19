package ru.rbkdev.rent.ui.navigation

import ru.rbkdev.rent.R

import android.os.Bundle
import android.text.InputType
import android.content.pm.PackageManager

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.EditTextPreference

/***/
class CSettingsFragment : PreferenceFragmentCompat() {

    /***/
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_fragment, rootKey)

        val ip = findPreference<EditTextPreference>("ip")
        val port = findPreference<EditTextPreference>("port")
        val timeout = findPreference<EditTextPreference>("timeout")

        ip?.setOnBindEditTextListener { editText ->
            editText.selectAll()
        }

        port?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            editText.selectAll()
        }

        timeout?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            editText.selectAll()
        }

        val version = findPreference<Preference>("version")

        try {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).let {

                version?.setSummary(it.versionName)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }
}