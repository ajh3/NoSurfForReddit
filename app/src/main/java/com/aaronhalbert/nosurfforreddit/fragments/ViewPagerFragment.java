package com.aaronhalbert.nosurfforreddit.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.TimeInterpolator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.adapters.NoSurfFragmentPagerAdapter;
import com.aaronhalbert.nosurfforreddit.viewmodel.NoSurfViewModel;
import com.google.android.material.tabs.TabLayout;

import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LAUNCH_ABOUT_EVENT;
import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LAUNCH_PREFS_EVENT;
import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LOGIN_EVENT;
import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents.VIEW_PAGER_FRAGMENT_LOGOUT_EVENT;

/* the main content fragment which holds all others, at the root of the activity's view */

public class ViewPagerFragment extends BaseFragment {
    private NoSurfViewModel viewModel;
    private boolean isUserLoggedIn = false;
    private Animator refreshDrawableAnimator;

    public static ViewPagerFragment newInstance() {
        return new ViewPagerFragment();
    }

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(requireActivity()).get(NoSurfViewModel.class);
        observeIsUserLoggedInLiveData();
        observeBothPostsViewStateLiveData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* inflate Animator in onCreateView(), as we are going to null it out in onDestroyView().
         * Keeps actions paired in corresponding lifecycle methods. */
        refreshDrawableAnimator = AnimatorInflater.loadAnimator(getContext(), R.animator.infinite_rotation);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // avoid leaking Context when ViewPagerFragment gets replace()'d and goes on backstack
        refreshDrawableAnimator = null;
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
        ViewPager pager = getView().findViewById(R.id.view_pager_fragment_pager);
        ImageView iv = (ImageView) menu.findItem(R.id.refresh).getActionView();
        iv.setImageResource(R.drawable.ic_refresh_24dp);
        refreshDrawableAnimator.setTarget(iv);
        refreshDrawableAnimator.setInterpolator(new OvershootInterpolator());
        iv.setOnClickListener(v -> {
            refreshDrawableAnimator.start();

            if (pager.getCurrentItem() == 0) {
                viewModel.fetchAllPostsASync();
            } else {
                viewModel.fetchSubscribedPostsASync();
            }
        });
    }

    // endregion menu ------------------------------------------------------------------------------

    // region observers ----------------------------------------------------------------------------

    /* Observe both viewstates, as the refresh button may refresh either data stream depending
     * on which page in the ViewPager is currently selected. */
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
