package com.mikechoch.prism.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.fragments.MainContentFragment;
import com.mikechoch.prism.fragments.NotificationsFragment;
import com.mikechoch.prism.fragments.ProfileFragment;
import com.mikechoch.prism.fragments.SearchFragment;

/**
 * Created by mikechoch on 1/22/18.
 */

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    /*
     * Global variables
     */
    private int NUM_ITEMS = Default.MAIN_VIEW_PAGER_SIZE;


    public MainViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MainContentFragment().newInstance();
//            case 1:
//                return new TrendingContentFragment().newInstance();
            case 2 - 1:
                return new SearchFragment().newInstance();
            case 3 - 1:
                return new NotificationsFragment().newInstance();
            case 4 - 1:
                return new ProfileFragment().newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

}
