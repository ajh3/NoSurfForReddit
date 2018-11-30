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
import com.aaronhalbert.nosurfforreddit.repository.SettingsStore;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewPagerFragmentViewModel;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.GotoUrlGlobalAction;
import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.gotoUrlGlobalAction;
import static com.aaronhalbert.nosurfforreddit.repository.NoSurfAuthenticator.buildAuthUrl;

/* the main content fragment which holds all others, at the root of the activity's view */

public class ViewPagerFragment extends BaseFragment {
    @SuppressWarnings("WeakerAccess") @Inject ViewModelFactory viewModelFactory;
    @Inject SettingsStore settingsStore;
    private ViewPagerFragmentViewModel viewModel;
    private MainActivityViewModel mainActivityViewModel;
    private boolean isUserLoggedIn = false;

    private ViewPager pager;
    private NavController navController;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(ViewPagerFragmentViewModel.class);
        mainActivityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        observeIsUserLoggedInLiveData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        setupViewPagerWithTabLayout(view);
        setPage();
        setupSplashVisibilityToggle();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        /* prevent memory leaks due to fragment going on backstack while retaining these objects
         * in instance variables. See comments on PostsFragment.onDestroyView() for a more detailed
         * explanation of this leak. */
        pager = null;
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
     * However, this does not appear to honor animation transitions defined in XML, so we
     * perform a manual navigate-by-action instead. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                mainActivityViewModel.logUserOut();
                return true;
            case R.id.login:
                launchLoginScreen();
                return true;
            case R.id.settings:
                launchPrefsScreen();
                return true;
            case R.id.about:
                launchAboutScreen();
                return true;
            // case R.id.refresh: handled in PostsFragment
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem loginMenuItem = menu.findItem(R.id.login);
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

    private void observeIsUserLoggedInLiveData() {
        viewModel.getIsUserLoggedInLiveData()
                .observe(this, loggedInStatus -> isUserLoggedIn = loggedInStatus);
    }

    /* allow splash animation to work correctly by ensuring RecyclerView is visible when
     * going BACK from a PostFragment. Necessary because this fragment's view hierarchy
     * is GONE by default to make the splash screen show over a totally blank background
     * on initial app startup. We toggle it VISIBLE whenever data are available. */
    private void setupSplashVisibilityToggle() {
        viewModel.getAllPostsViewStateLiveData().observe(getViewLifecycleOwner(), postsViewState ->
                getView().findViewById(R.id.view_pager_fragment_base_view).setVisibility(View.VISIBLE));
    }

    // endregion observers -------------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void setupViewPagerWithTabLayout(View view) {
        pager = view.findViewById(R.id.view_pager_fragment_pager);
        TabLayout tabs = view.findViewById(R.id.view_pager_fragment_tabs);
        NoSurfFragmentPagerAdapter noSurfFragmentPagerAdapter =
                new NoSurfFragmentPagerAdapter(getChildFragmentManager());

        pager.setAdapter(noSurfFragmentPagerAdapter);
        tabs.setupWithViewPager(pager);
        tabs.setTabMode(TabLayout.MODE_FIXED);
    }

    private void setPage() {
        if (settingsStore.isDefaultPageSubscribed()) {
            pager.setCurrentItem(1);
        }
    }

    // endregion helper methods --------------------------------------------------------------------

    // region navigation helper methods ------------------------------------------------------------

    private void launchLoginScreen() {
        GotoUrlGlobalAction action
                = gotoUrlGlobalAction(buildAuthUrl());

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
