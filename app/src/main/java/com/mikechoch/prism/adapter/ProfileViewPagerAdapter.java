package com.mikechoch.prism.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.fragments.LikedPostsFragment;
import com.mikechoch.prism.fragments.MainContentFragment;
import com.mikechoch.prism.fragments.NotificationsFragment;
import com.mikechoch.prism.fragments.ProfileFragment;
import com.mikechoch.prism.fragments.SearchFragment;
import com.mikechoch.prism.fragments.UploadedRepostedPostsFragment;

/**
 * Created by mikechoch on 1/22/18.
 */

public class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {

    /*
     * Global variables
     */
    private int NUM_ITEMS = Default.USER_POSTS_VIEW_PAGER_SIZE;


    public ProfileViewPagerAdapter(FragmentManager fragmentManager) {
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
                return new UploadedRepostedPostsFragment().newInstance();
            case 1:
                return new LikedPostsFragment().newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

}
