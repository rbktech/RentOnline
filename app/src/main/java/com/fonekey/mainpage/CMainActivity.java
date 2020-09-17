package com.fonekey.mainpage;
import com.fonekey.R;

import android.os.Bundle;
import android.content.Intent;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;

public class CMainActivity extends AppCompatActivity {

    public CMainActivity() {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.mainViewPager);

        viewPager.setAdapter(new CPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}