package com.aaronhalbert.nosurfforreddit.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.fragments.AllPostsFragment;
import com.aaronhalbert.nosurfforreddit.fragments.HomePostsFragment;

public class NoSurfFragmentPagerAdapter extends FragmentPagerAdapter {
    static final int NUM_ITEMS = 2;

    public NoSurfFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        Log.e(getClass().toString(), fm.toString());
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return AllPostsFragment.newInstance("abc", "def");
        } else {
            return HomePostsFragment.newInstance("abc", "def");
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "/r/All";
        } else {
            return "Yours";
        }
    }
}