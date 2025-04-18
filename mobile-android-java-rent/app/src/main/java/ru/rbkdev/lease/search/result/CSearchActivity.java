package ru.rbkdev.lease.search.result;
import ru.rbkdev.lease.R;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import ru.rbkdev.lease.search.result.pageradapter.CSearchPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class CSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search);

        TabLayout tabLayout = findViewById(R.id.searchTabLayout);
        ViewPager viewPager = findViewById(R.id.searchViewPager);

        viewPager.setAdapter(new CSearchPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }
}