package com.aaronhalbert.nosurfforreddit.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.fragments.AllPostsFragment;
import com.aaronhalbert.nosurfforreddit.fragments.LoginFragment;
import com.aaronhalbert.nosurfforreddit.fragments.SubscribedPostsFragment;

public class NoSurfFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private static final int NUM_ITEMS = 2;

    NoSurfViewModel viewModel;

    public NoSurfFragmentPagerAdapter(FragmentManager fm, NoSurfViewModel viewModel) {
        super(fm);
        this.viewModel = viewModel;
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        Log.e(getClass().toString(), "getItem called");

        if (position == 0) {
            return AllPostsFragment.newInstance("abc", "def");
        } else if ((position == 1) && (viewModel.isUserLoggedIn())) {
            return SubscribedPostsFragment.newInstance("abc", "def");
        } else {
            return LoginFragment.newInstance("abc", "def");
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

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
