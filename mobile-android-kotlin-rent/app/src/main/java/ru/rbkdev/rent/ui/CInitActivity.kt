package ru.rbkdev.rent.ui

import android.os.Bundle
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class CInitActivity : AppCompatActivity() {

    private var mResultLauncher: ActivityResultLauncher<Intent>? = null

    /***/
    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)

        mResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            finish()
        }

        mResultLauncher?.launch(Intent(baseContext, CMainActivity::class.java))
    }
}