package com.aaronhalbert.nosurfforreddit.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
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

public class ViewPagerFragment extends BaseFragment {
    private ViewPager pager;
    private NoSurfViewModel viewModel;
    private boolean isUserLoggedIn = false;
    private ViewPagerFragmentInteractionListener viewPagerFragmentInteractionListener;
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
        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);
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
        NoSurfFragmentPagerAdapter noSurfFragmentPagerAdapter = new NoSurfFragmentPagerAdapter(getChildFragmentManager());

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
                    viewModel.fetchAllPostsSync();
                } else {
                    viewModel.fetchSubscribedPostsSync();
                }
                return true;
            case R.id.login:
                login();
                return true;
            case R.id.logout:
                logout();
                return true;
            case R.id.settings:
                launchPreferencesScreen();
                return true;
            case R.id.about:
                launchAboutScreen();
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
        viewModel.getIsUserLoggedInLiveData().observe(this, loggedInStatus -> {
            isUserLoggedIn = loggedInStatus;
        });
    }

    private void login() {
        if (viewPagerFragmentInteractionListener != null) {
            viewPagerFragmentInteractionListener.login();
        }
    }

    private void logout() {
        if (viewPagerFragmentInteractionListener != null) {
            viewPagerFragmentInteractionListener.logout();
        }
    }

    private void launchPreferencesScreen() {
        if (viewPagerFragmentInteractionListener != null) {
            viewPagerFragmentInteractionListener.launchPreferencesScreen();
        }
    }

    private void launchAboutScreen() {
        if (viewPagerFragmentInteractionListener != null) {
            viewPagerFragmentInteractionListener.launchAboutScreen();
        }
    }

    // endregion helper methods --------------------------------------------------------------------

    // region interfaces ---------------------------------------------------------------------------

    public interface ViewPagerFragmentInteractionListener {
        void login();
        void logout();
        void launchPreferencesScreen();
        void launchAboutScreen();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ViewPagerFragmentInteractionListener) {
            viewPagerFragmentInteractionListener = (ViewPagerFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ViewPagerFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        viewPagerFragmentInteractionListener = null;
    }

    // endregion interfaces ------------------------------------------------------------------------
}
