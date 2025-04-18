package ru.rbkdev.lease.main;

import ru.rbkdev.lease.R;

import com.google.android.material.navigation.NavigationView;

import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

public class CMainActivity extends AppCompatActivity {

    private static final int m_layout = R.layout.activity_main;

    /*static {
        System.loadLibrary("native-lib");
    }*/

    public static String g_userId = CID.USER_EMPTY;
    public static CClient g_client;
    public static Map<Integer, String> g_commandError;

    private AppBarConfiguration mAppBarConfiguration;
    DrawerLayout m_drawerLayout;
    NavigationView m_navigationView;
    NavController m_navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(m_layout);

        {
            g_client = new CClient();
            g_commandError = new HashMap<>();

            g_commandError.put(CLISTID.COMMAND_ERROR, "NOT DONE");
            g_commandError.put(CLISTID.COMMAND_NOT_FOUND, "NOT_FOUND");
        }

        RefreshUserId(new File(getExternalFilesDir(null), CID.FILE_NAME), "A");

        m_drawerLayout = findViewById(R.id.drawerLayout);
        m_navigationView = findViewById(R.id.navigationView);

        m_navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        m_navController.setGraph(R.navigation.mobile_navigation);
        mAppBarConfiguration = new AppBarConfiguration.Builder(m_navController.getGraph()).setOpenableLayout(m_drawerLayout).build();

        NavigationUI.setupActionBarWithNavController(this, m_navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(m_navigationView, m_navController);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(resultCode != RESULT_CANCELED) {
            if(requestCode == CID.RESULT_RETURN_LOGIN)
                m_navController.popBackStack();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        g_client.Close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    public static void RefreshUserId(File file, String writeArray) {

        try {

            if(!file.exists())
                if(!file.createNewFile())
                    return;

            BufferedReader reader = new BufferedReader(new FileReader(file));
            while((g_userId = reader.readLine()) == null) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(writeArray);
                writer.close();
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void RefreshAccount(String text)
    {
        File file = new File(getExternalFilesDir(null), CID.FILE_NAME);
        if(file.exists() && file.delete()) {
            RefreshUserId(file, text);
            this.onBackPressed();
        }
    }
}