package com.aaronhalbert.nosurfforreddit.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.aaronhalbert.nosurfforreddit.fragments.AllPostsFragment;
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
            return AllPostsFragment.newInstance();
        } else {
            return ContainerFragment.newInstance();
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
