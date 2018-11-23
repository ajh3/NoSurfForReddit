package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.adapters.NoSurfFragmentPagerAdapter;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewPagerFragmentViewModel;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import static com.aaronhalbert.nosurfforreddit.repository.NoSurfAuthenticator.buildAuthUrl;

/* the main content fragment which holds all others, at the root of the activity's view */

public class ViewPagerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @SuppressWarnings("WeakerAccess") @Inject ViewModelFactory viewModelFactory;
    private ViewPagerFragmentViewModel viewModel;
    private MainActivityViewModel mainActivityViewModel;
    private boolean isUserLoggedIn = false;

    private ViewPager pager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NavController navController;

    public static ViewPagerFragment newInstance() {
        return new ViewPagerFragment();
    }

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(ViewPagerFragmentViewModel.class);
        mainActivityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        observeIsUserLoggedInLiveData();
        observeBothPostsViewStateLiveData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_view_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSwipeRefreshLayout(view);
        navController = Navigation.findNavController(view);

        pager = view.findViewById(R.id.view_pager_fragment_pager);
        TabLayout tabs = view.findViewById(R.id.view_pager_fragment_tabs);
        NoSurfFragmentPagerAdapter noSurfFragmentPagerAdapter =
                new NoSurfFragmentPagerAdapter(getChildFragmentManager());

        pager.setAdapter(noSurfFragmentPagerAdapter);
        tabs.setupWithViewPager(pager);
        tabs.setTabMode(TabLayout.MODE_FIXED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        /* prevent memory leaks due to fragment going on backstack while retaining these
         * in instance variables. See commends on PostsFragment.onDestroyView() for a more detailed
         * explanation of this leak. */
        pager = null;
        swipeRefreshLayout = null;
        navController = null;
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region menu ---------------------------------------------------------------------------------

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /* Nav component documentation says to handle menu clicks like this:
     *
     * return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(getView()));
     *
     * However, this method does not appear to honor animation transitions defined in XML, so we
     * perform a standard navigate-by-action instead. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                mainActivityViewModel.logUserOut();
                return true;
            case R.id.goto_url_action:
                launchLoginScreen();
                return true;
            case R.id.goto_prefs_action:
                launchPrefsScreen();
                return true;
            case R.id.goto_about_action:
                launchAboutScreen();
                return true;
            case R.id.refresh:
                refresh();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem loginMenuItem = menu.findItem(R.id.goto_url_action);
        MenuItem logoutMenuItem = menu.findItem(R.id.logout);

        if (isUserLoggedIn) {
            loginMenuItem.setVisible(false);
            logoutMenuItem.setVisible(true);
        } else {
            loginMenuItem.setVisible(true);
            logoutMenuItem.setVisible(false);
        }
    }

    // endregion menu ------------------------------------------------------------------------------

    // region observers ----------------------------------------------------------------------------

    /* We need to observe both viewstates, as the refresh button may refresh either data stream
     * depending on which ViewPager page is currently selected. */
    private void observeBothPostsViewStateLiveData() {
        viewModel.getAllPostsViewStateLiveData()
                .observe(this, listing -> cancelRefreshingAnimation());
        viewModel.getSubscribedPostsViewStateLiveData()
                .observe(this, listing -> cancelRefreshingAnimation());
    }

    private void observeIsUserLoggedInLiveData() {
        viewModel.getIsUserLoggedInLiveData()
                .observe(this, loggedInStatus -> isUserLoggedIn = loggedInStatus);
    }

    /* onRefresh is called directly when the user swipes to refresh. It is also called indirectly
     * when the user clicks "Refresh" in the menu, by way of refresh() triggering onRefresh()
     * by posting a Runnable and turning on the animation manually. */
    @Override
    public void onRefresh() {
        if (pager.getCurrentItem() == 0) {
            viewModel.fetchAllPostsASync();
        } else if ((pager.getCurrentItem() == 1) && isUserLoggedIn) {
            viewModel.fetchSubscribedPostsASync();
        }
    }

    // endregion observers -------------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void refresh() {
        if (pager.getCurrentItem() == 0) {
            swipeRefreshLayout.post(() -> {
                swipeRefreshLayout.setRefreshing(true);
                ViewPagerFragment.this.onRefresh();
            });
        } else if ((pager.getCurrentItem() == 1) && isUserLoggedIn) {
            swipeRefreshLayout.post(() -> {
                swipeRefreshLayout.setRefreshing(true);
                ViewPagerFragment.this.onRefresh();
            });
        }
    }

    private void setupSwipeRefreshLayout(View v) {
        swipeRefreshLayout = v.findViewById(R.id.view_pager_fragment_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void cancelRefreshingAnimation() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    // endregion helper methods --------------------------------------------------------------------

    // region navigation helper methods ------------------------------------------------------------

    private void launchLoginScreen() {
        ViewPagerFragmentDirections.GotoUrlAction action
                = ViewPagerFragmentDirections.gotoUrlAction(buildAuthUrl());

        navController.navigate(action);
    }

    private void launchPrefsScreen() {
        ViewPagerFragmentDirections.GotoPrefsAction action
                = ViewPagerFragmentDirections.gotoPrefsAction();

        navController.navigate(action);
    }

    private void launchAboutScreen() {
        ViewPagerFragmentDirections.GotoAboutAction action
                = ViewPagerFragmentDirections.gotoAboutAction();

        navController.navigate(action);
    }

    // endregion navigation helper methods ---------------------------------------------------------
}
