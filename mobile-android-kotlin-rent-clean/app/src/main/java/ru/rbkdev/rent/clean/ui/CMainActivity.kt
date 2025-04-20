package ru.rbkdev.rent.clean.ui

import ru.rbkdev.rent.clean.R

import ru.rbkdev.rent.clean.Globals

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu

import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.findNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import com.google.android.material.navigation.NavigationView

import com.yandex.mapkit.MapKitFactory

/***/
class CMainActivity : AppCompatActivity() {

    /** GUI */
    // -------------------------------------------------------------------

    private lateinit var mAppBarConfiguration: AppBarConfiguration

    /***/
    override fun onCreate(savedInstanceState: Bundle?) {

        MapKitFactory.setApiKey("MapKey")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_view)

        Globals.APP_CONTEXT = applicationContext

        /** GUI */
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        val navigationController = findNavController(R.id.navigationHostContainer)
        mAppBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigationMap,
                // R.id.navigationHousesList,
                R.id.navigationKeysList,
                R.id.navigationSettings,
            ), drawerLayout
        )

        setupActionBarWithNavController(navigationController, mAppBarConfiguration)
        navigationView.setupWithNavController(navigationController)

        drawerLayout.openDrawer(GravityCompat.START)
        Handler(Looper.getMainLooper()).postDelayed({
            drawerLayout.closeDrawer(GravityCompat.START)
        }, 500)
    }

    /***/
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    /***/
    override fun onSupportNavigateUp(): Boolean {

        val navController = findNavController(R.id.navigationHostContainer)
        return navController.navigateUp(mAppBarConfiguration) || super.onSupportNavigateUp()
    }
}