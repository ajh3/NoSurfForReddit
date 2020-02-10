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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.aaronhalbert.nosurfforreddit.BaseFragment;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.data.local.settings.PreferenceSettingsStore;
import com.aaronhalbert.nosurfforreddit.data.remote.auth.AuthenticatorUtils;
import com.aaronhalbert.nosurfforreddit.ui.master.AllPostsFragment;
import com.aaronhalbert.nosurfforreddit.ui.master.ContainerFragment;
import com.aaronhalbert.nosurfforreddit.utils.ViewModelFactory;
import com.google.android.material.tabs.TabLayout;

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

    private NavController navController;
    private FragmentManager fm;

    private TabLayout tabs;

    private static final String TAG_CONTAINER_FRAGMENT = "containerFragment";
    private static final String TAG_ALL_POSTS_FRAGMENT = "allPostsFragment";

    private static final String TAB_STATE = "tabState";

    private static final String YOUR_SUBREDDITS = "Your Subreddits";
    private static final String R_ALL = "/r/All";

    private int tabPosition = 0;


    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
                .get(MainActivityViewModel.class);
        fm = getChildFragmentManager();

        if (findContainerFragment() == null) {

            fm
                    .beginTransaction()
                    .add(
                            R.id.asdf,
                            ContainerFragment.newInstance(),
                            TAG_CONTAINER_FRAGMENT)
                    .commitNow();
        }

        if (findAllPostsFragment() == null) {

            fm
                    .beginTransaction()
                    .add(
                            R.id.asdf,
                            AllPostsFragment.newInstance(),
                            TAG_ALL_POSTS_FRAGMENT)
                    .commitNow();
        }



        if (savedInstanceState == null) {
            if (preferenceSettingsStore.isDefaultPageAll()) {
                tabPosition = 1;
            }
        } else {
            tabPosition = savedInstanceState.getInt(TAB_STATE, 0);
        }

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
        setupViewPager(view);


        setPage();


        setupSplashVisibilityToggle();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        /* prevent memory leaks due to fragment going on backstack while retaining these objects
         * in instance variables. See comments on PostsFragment.onDestroyView() for a more detailed
         * explanation of this leak. */
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(TAB_STATE, tabPosition);
    }

    // endregion menu ------------------------------------------------------------------------------

    // region observers ----------------------------------------------------------------------------

    private void observeIsUserLoggedInLiveData() {
        viewModel.getIsUserLoggedInLiveData()
                .observe(this, loggedInStatus -> {
                    isUserLoggedIn = loggedInStatus;

                    FragmentTransaction ft = fm.beginTransaction();

                    if (tabs.getSelectedTabPosition() == 0) {
                        ft.show(findContainerFragment()).hide(findAllPostsFragment()).commit();


                    } else {
                        ft.hide(findContainerFragment()).show(findAllPostsFragment()).commit();

                    }
        });
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

    private void setupViewPager(View view) {
        tabs = view.findViewById(R.id.view_pager_fragment_tabs);


        tabs.addTab(tabs.newTab().setText(YOUR_SUBREDDITS));
        tabs.addTab(tabs.newTab().setText(R_ALL));

        tabs.setTabMode(TabLayout.MODE_FIXED);


        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                // not automatically restored for some reason
                tabPosition = tabs.getSelectedTabPosition();

                FragmentTransaction ft = fm.beginTransaction();

                if (tabs.getSelectedTabPosition() == 0) {
                    ft.show(findContainerFragment()).hide(findAllPostsFragment()).commit();


                } else {
                    ft.hide(findContainerFragment()).show(findAllPostsFragment()).commit();

                }



            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    //TODO: file bug report with Google?
    /*
     * Disabled due to ViewPager & ViewPager2 not correctly working with setCurrentItem()
     * during initial setup.
     *
     * https://stackoverflow.com/questions/19316729/android-viewpager-setcurrentitem-not-working-after-onresume
     *
     */
    private void setPage() {

        tabs.getTabAt(tabPosition).select();


    }



    private Fragment findAllPostsFragment() {
        return fm.findFragmentByTag(TAG_ALL_POSTS_FRAGMENT);
    }

    private Fragment findContainerFragment() {
        return fm.findFragmentByTag(TAG_CONTAINER_FRAGMENT);
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
