package com.aaronhalbert.nosurfforreddit.fragments;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.R;

/* This fragment is necessary only because it's difficult to directly swap pages inside a
 * FragmentPagerAdapter.
 *
 * When the user is logged out, it displays a LoginFragment to
 * prompt the user to log in, and when the user is logged in, it displays the user's
 * subscribed posts. */

public class ContainerFragment extends BaseFragment {
    private static final String TAG_SUBSCRIBED_POSTS_FRAGMENT = "subscribedPostsFragment";
    private static final String TAG_LOGIN_FRAGMENT = "loginFragment";

    private MainActivityViewModel viewModel;
    private FragmentManager fm;

    public static ContainerFragment newInstance() {
        return new ContainerFragment();
    }

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        fm = getChildFragmentManager();

        // we add both fragments and simply show/hide them as needed
        if (findLoginFragment() == null) {
            fm
                    .beginTransaction()
                    .add(
                            R.id.container_fragment_base_view,
                            LoginFragment.newInstance(),
                            TAG_LOGIN_FRAGMENT)
                    .commit();
        }

        if (findSubscribedPostsFragment() == null) {
            fm
                    .beginTransaction()
                    .add(
                            R.id.container_fragment_base_view,
                            SubscribedPostsFragment.newInstance(),
                            TAG_SUBSCRIBED_POSTS_FRAGMENT)
                    .commit();
        }

        observeIsUserLoggedInLiveData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_container, container, false);
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void observeIsUserLoggedInLiveData() {
        viewModel.getIsUserLoggedInLiveData().observe(this, isUserLoggedIn -> {
            FragmentTransaction ft = fm.beginTransaction();

            if (isUserLoggedIn) {
                ft.hide(findLoginFragment()).show(findSubscribedPostsFragment()).commit();
            } else {
                ft.show(findLoginFragment()).hide(findSubscribedPostsFragment()).commit();
            }
        });
    }

    private Fragment findLoginFragment() {
        return fm.findFragmentByTag(TAG_LOGIN_FRAGMENT);
    }

    private Fragment findSubscribedPostsFragment() {
        return fm.findFragmentByTag(TAG_SUBSCRIBED_POSTS_FRAGMENT);
    }

    // endregion helper methods --------------------------------------------------------------------
}
