package com.aaronhalbert.nosurfforreddit.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.aaronhalbert.nosurfforreddit.fragments.PostsFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ContainerFragment;

public class NoSurfFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_ITEMS = 2;

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
            return PostsFragment.newInstance(false);
        } else {
            return ContainerFragment.newInstance("abc", "def");
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "/r/All";
        } else {
            return "Your Subreddits";
        }
    }
}
