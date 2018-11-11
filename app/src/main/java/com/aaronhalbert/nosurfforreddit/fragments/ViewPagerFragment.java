package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.aaronhalbert.nosurfforreddit.viewmodel.NoSurfViewModel;
import com.google.android.material.tabs.TabLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.adapters.NoSurfFragmentPagerAdapter;

import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LAUNCH_ABOUT_EVENT;
import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LAUNCH_PREFS_EVENT;
import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LOGIN_EVENT;
import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LOGOUT_EVENT;

/* the main content fragment which holds all others, at the root of the activity's view */

public class ViewPagerFragment extends BaseFragment {
    private ViewPager pager;
    private NoSurfViewModel viewModel;
    private boolean isUserLoggedIn = false;
    private MenuItem loginMenuItem;
    private MenuItem logoutMenuItem;

    public static ViewPagerFragment newInstance() {
        return new ViewPagerFragment();
    }

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(getClass().toString(), "ViewPagerFragment onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(requireActivity()).get(NoSurfViewModel.class);
        observeIsUserLoggedInLiveData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_view_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pager = view.findViewById(R.id.view_pager_fragment_pager);
        TabLayout tabs = view.findViewById(R.id.view_pager_fragment_tabs);
        NoSurfFragmentPagerAdapter noSurfFragmentPagerAdapter =
                new NoSurfFragmentPagerAdapter(getChildFragmentManager());

        pager.setAdapter(noSurfFragmentPagerAdapter);
        tabs.setupWithViewPager(pager);
        tabs.setTabMode(TabLayout.MODE_FIXED);
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region menu ---------------------------------------------------------------------------------

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view_pager_fragment, menu);

        loginMenuItem = menu.findItem(R.id.login);
        logoutMenuItem = menu.findItem(R.id.logout);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.refresh:
                if (pager.getCurrentItem() == 0) {
                    viewModel.fetchAllPostsASync();
                } else {
                    viewModel.fetchSubscribedPostsASync();
                }
                return true;
            case R.id.login:
                viewModel.setViewPagerFragmentClickEventsLiveData(VIEW_PAGER_FRAGMENT_LOGIN_EVENT);
                return true;
            case R.id.logout:
                viewModel.setViewPagerFragmentClickEventsLiveData(VIEW_PAGER_FRAGMENT_LOGOUT_EVENT);
                return true;
            case R.id.settings:
                viewModel.setViewPagerFragmentClickEventsLiveData(VIEW_PAGER_FRAGMENT_LAUNCH_PREFS_EVENT);
                return true;
            case R.id.about:
                viewModel.setViewPagerFragmentClickEventsLiveData(VIEW_PAGER_FRAGMENT_LAUNCH_ABOUT_EVENT);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (isUserLoggedIn) {
            loginMenuItem.setVisible(false);
            logoutMenuItem.setVisible(true);
        } else {
            loginMenuItem.setVisible(true);
            logoutMenuItem.setVisible(false);
        }
    }

    // endregion menu ------------------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void observeIsUserLoggedInLiveData() {
        viewModel.getIsUserLoggedInLiveData()
                .observe(this, loggedInStatus -> isUserLoggedIn = loggedInStatus);
    }

    // endregion helper methods --------------------------------------------------------------------

    // region enums --------------------------------------------------------------------------------

    public enum ViewPagerFragmentNavigationEvents {
        VIEW_PAGER_FRAGMENT_LOGIN_EVENT,
        VIEW_PAGER_FRAGMENT_LOGOUT_EVENT,
        VIEW_PAGER_FRAGMENT_LAUNCH_PREFS_EVENT,
        VIEW_PAGER_FRAGMENT_LAUNCH_ABOUT_EVENT
    }

    // endregion enums -----------------------------------------------------------------------------
}
