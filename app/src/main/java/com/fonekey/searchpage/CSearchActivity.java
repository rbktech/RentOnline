package com.fonekey.searchpage;
import com.fonekey.R;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;

public class CSearchActivity extends AppCompatActivity {

    public CSearchActivity() {};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.searchTabLayout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.searchViewPager);

        viewPager.setAdapter(new CSearchPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }
}