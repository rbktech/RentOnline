package ru.rbkdev.lease.search.result.pageradapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import ru.rbkdev.lease.search.result.pageradapter.listfarm.CListFarmFragment;

public class CSearchPagerAdapter extends FragmentPagerAdapter {

    private String[] tabs;

    public CSearchPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);

        tabs = new String[] { "Квартиры", "Фильтр", "Карта" };
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new CListFarmFragment();
            case 1:
                return new CFilterFragment();
            case 2:
                return new CMapsFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return tabs.length;
    }
}
