package com.aaronhalbert.nosurfforreddit.adapters;

import com.aaronhalbert.nosurfforreddit.fragments.AllPostsFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ContainerFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class NoSurfFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_ITEMS = 2;
    private static final String R_ALL = "/r/All";
    private static final String YOUR_SUBREDDITS = "Your Subreddits";

    public NoSurfFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return AllPostsFragment.newInstance();
        } else {
            return ContainerFragment.newInstance();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return R_ALL;
        } else {
            return YOUR_SUBREDDITS;
        }
    }
}
