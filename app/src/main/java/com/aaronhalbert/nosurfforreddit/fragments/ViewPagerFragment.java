package com.aaronhalbert.nosurfforreddit.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.adapters.NoSurfFragmentPagerAdapter;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewPagerFragmentViewModel;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LAUNCH_ABOUT_EVENT;
import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LAUNCH_PREFS_EVENT;
import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LOGIN_EVENT;
import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LOGOUT_EVENT;

/* the main content fragment which holds all others, at the root of the activity's view */

public class ViewPagerFragment extends BaseFragment {
    @SuppressWarnings("WeakerAccess") @Inject ViewModelFactory viewModelFactory;
    private ViewPagerFragmentViewModel viewModel;
    private MainActivityViewModel mainActivityViewModel;
    private boolean isUserLoggedIn = false;
    private Animator refreshDrawableAnimator;

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

        /* inflate Animator in onCreateView(), as we are going to null it out in onDestroyView().
         * Keeps actions paired in corresponding lifecycle methods. */
        refreshDrawableAnimator = AnimatorInflater.loadAnimator(getContext(), R.animator.refresh_button_rotation);

        return inflater.inflate(R.layout.fragment_view_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager pager = view.findViewById(R.id.view_pager_fragment_pager);
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
        setupRefreshIconAnimation(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.refresh:
                /* N/A. The refresh button is "overridden" by setupRefreshIconAnimation()
                 * in order to animate it. */
                return true;
            case R.id.login:
                mainActivityViewModel.setViewPagerFragmentClickEventsLiveData(VIEW_PAGER_FRAGMENT_LOGIN_EVENT);
                return true;
            case R.id.logout:
                mainActivityViewModel.setViewPagerFragmentClickEventsLiveData(VIEW_PAGER_FRAGMENT_LOGOUT_EVENT);
                return true;
            case R.id.settings:
                mainActivityViewModel.setViewPagerFragmentClickEventsLiveData(VIEW_PAGER_FRAGMENT_LAUNCH_PREFS_EVENT);
                return true;
            case R.id.about:
                mainActivityViewModel.setViewPagerFragmentClickEventsLiveData(VIEW_PAGER_FRAGMENT_LAUNCH_ABOUT_EVENT);
                return true;
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

    /* The refresh button does not have an icon assigned to it in XML. Instead, we use its
     * actionViewClass attribute to assign a drawable, so that we can treat it as an ImageView
     * and more easily animate it when clicked. */
    private void setupRefreshIconAnimation(Menu menu) {
        ImageView iv = (ImageView) menu.findItem(R.id.refresh).getActionView();
        iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_refresh_24dp));
        refreshDrawableAnimator.setTarget(iv);
        iv.setOnClickListener(v -> {
            refreshDrawableAnimator.start();
            ViewPager pager = getView().findViewById(R.id.view_pager_fragment_pager);

            if (pager.getCurrentItem() == 0) {
                viewModel.fetchAllPostsASync();
            } else {
                if (isUserLoggedIn) {
                    viewModel.fetchSubscribedPostsASync();
                } else {
                    refreshDrawableAnimator.end();
                }
            }
        });
    }

    // endregion menu ------------------------------------------------------------------------------

    // region observers ----------------------------------------------------------------------------

    /* We need to observe both viewstates, as the refresh button may refresh either data stream
     * depending on which ViewPager page is currently selected. */
    private void observeBothPostsViewStateLiveData() {
        viewModel.getAllPostsViewStateLiveData().observe(this, listing -> refreshDrawableAnimator.end());
        viewModel.getSubscribedPostsViewStateLiveData().observe(this, listing -> refreshDrawableAnimator.end());
    }

    private void observeIsUserLoggedInLiveData() {
        viewModel.getIsUserLoggedInLiveData()
                .observe(this, loggedInStatus -> isUserLoggedIn = loggedInStatus);
    }

    // endregion observers -------------------------------------------------------------------------

    // region enums --------------------------------------------------------------------------------

    public enum ViewPagerFragmentNavigationEvents {
        VIEW_PAGER_FRAGMENT_LOGIN_EVENT,
        VIEW_PAGER_FRAGMENT_LOGOUT_EVENT,
        VIEW_PAGER_FRAGMENT_LAUNCH_PREFS_EVENT,
        VIEW_PAGER_FRAGMENT_LAUNCH_ABOUT_EVENT
    }

    // endregion enums -----------------------------------------------------------------------------
}
