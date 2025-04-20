package ru.rbkdev.rent.clean.ui.login

import ru.rbkdev.rent.clean.R

import ru.rbkdev.rent.clean.CClient
import ru.rbkdev.rent.clean.CDataExchange
import ru.rbkdev.rent.clean.ui.CMainActivity
import ru.rbkdev.rent.clean.CSettings

import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import android.widget.TextView
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import com.yandex.mapkit.MapKitFactory

/***/
class CLoginActivity : AppCompatActivity() {

    private var mResultLauncher: ActivityResultLauncher<Intent>? = null

    /***/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val txtLoginLogin: TextView = findViewById(R.id.txtLoginLogin)
        val txtLoginPassword: TextView = findViewById(R.id.txtLoginPassword)
        val btnLoginEntry: Button = findViewById(R.id.btnLoginEntry)

        var login = CSettings.getInstance().getLogin(baseContext)
        if (login != "null")
            txtLoginLogin.text = login

        var password = CSettings.getInstance().getPassword(baseContext)
        if (password != "null")
            txtLoginPassword.text = password

        btnLoginEntry.setOnClickListener {

            login = txtLoginLogin.text.toString()
            password = txtLoginPassword.text.toString()

            if (password == "ip") {
                val array = login.split(":")
                if (array.size == 2) {
                    CSettings.getInstance().setLinkPreference(baseContext, array[0], array[1])
                    Toast.makeText(baseContext, "success", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val dataExchange = CDataExchange()
            dataExchange.setCommand("login_clean")
            dataExchange.setIdUser("0")
            dataExchange.setIdHouse("0")
            dataExchange.setMessage("${txtLoginLogin.text}:${txtLoginPassword.text}")

            CClient.startExchange(baseContext, dataExchange)
            if (dataExchange.getCode() == R.string.code_success) {

                CSettings.getInstance().setUserId(dataExchange.getIdUser())
                CSettings.getInstance().setLoginPreference(baseContext, login, password)

                mResultLauncher?.launch(Intent(baseContext, CMainActivity::class.java))
            } else
                Toast.makeText(baseContext, "Неправильный ввод", Toast.LENGTH_SHORT).show()
        }

        mResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            finish()

            MapKitFactory.setApiKey("MapKey")
        }
    }
}