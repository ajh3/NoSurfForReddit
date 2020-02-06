/*
 * Copyright (c) 2018-present, Aaron J. Halbert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.aaronhalbert.nosurfforreddit.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.aaronhalbert.nosurfforreddit.BaseFragment;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.data.local.settings.PreferenceSettingsStore;
import com.aaronhalbert.nosurfforreddit.data.remote.auth.AuthenticatorUtils;
import com.aaronhalbert.nosurfforreddit.utils.ViewModelFactory;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import javax.inject.Inject;

import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.GotoLoginUrlGlobalAction;
import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.gotoLoginUrlGlobalAction;

/* the main content fragment which holds all others, at the root of the activity's view */

public class ViewPagerFragment extends BaseFragment {
    @Inject PreferenceSettingsStore preferenceSettingsStore;
    @Inject AuthenticatorUtils authenticatorUtils;
    @SuppressWarnings("WeakerAccess") @Inject ViewModelFactory viewModelFactory;
    private MainActivityViewModel viewModel;
    private boolean isUserLoggedIn = false;

    private ViewPager2 pager;
    private NavController navController;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
                .get(MainActivityViewModel.class);
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
                viewModel.logUserOut();
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
                new NoSurfFragmentPagerAdapter(this);

        pager.setAdapter(noSurfFragmentPagerAdapter);

        new TabLayoutMediator(tabs, pager,
                (tab, position) -> tab.setText(noSurfFragmentPagerAdapter.getTitle(position))).attach();

        tabs.setTabMode(TabLayout.MODE_FIXED);
    }

    private void setPage() {
        if (preferenceSettingsStore.isDefaultPageSubscribed()) {
            pager.setCurrentItem(1);
        }
    }

    // endregion helper methods --------------------------------------------------------------------

    // region navigation helper methods ------------------------------------------------------------

    private void launchLoginScreen() {
        GotoLoginUrlGlobalAction action
                = gotoLoginUrlGlobalAction(authenticatorUtils.buildAuthUrl());

        navController.navigate(action);
    }

    private void launchPrefsScreen() {
        NavDirections action = ViewPagerFragmentDirections.gotoPrefsAction();

        navController.navigate(action);
    }

    private void launchAboutScreen() {
        NavDirections action = ViewPagerFragmentDirections.gotoAboutAction();

        navController.navigate(action);
    }

    // endregion navigation helper methods ---------------------------------------------------------
}
