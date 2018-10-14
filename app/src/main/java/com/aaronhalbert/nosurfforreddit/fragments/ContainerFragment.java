package com.aaronhalbert.nosurfforreddit.fragments;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.R;

public class ContainerFragment extends Fragment {

    private static final String TAG_SUBSCRIBED_POSTS_FRAGMENT = "subscribedPostsFragment";
    private static final String TAG_LOGIN_FRAGMENT = "loginFragment";

    private NoSurfViewModel viewModel;

    public ContainerFragment() {
    }

    public static ContainerFragment newInstance() {
        ContainerFragment fragment = new ContainerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(NoSurfViewModel.class);

        viewModel.getUserOAuthRefreshTokenLiveData().observe(this, s -> {
            boolean isUserLoggedIn = (s != null) && !(s.equals(""));
            setContainerChildFragment(isUserLoggedIn);
        });

        boolean isUserLoggedIn = viewModel.isUserLoggedIn();
        setContainerChildFragment(isUserLoggedIn);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_container, container, false);
    }

    public void setContainerChildFragment(boolean isUserLoggedIn) {
        FragmentManager fm = getChildFragmentManager();
        Fragment loginFragment = fm.findFragmentByTag(TAG_LOGIN_FRAGMENT);
        Fragment subscribedPostsFragment = fm.findFragmentByTag(TAG_SUBSCRIBED_POSTS_FRAGMENT);

        if (isUserLoggedIn) {
            if (loginFragment != null) {
                if (subscribedPostsFragment != null) { // if both subscribedPostsFragment and loginFragment exist
                    fm.beginTransaction()
                            .remove(loginFragment)
                            .commit();
                } else { // if only loginFragment exists
                    fm.beginTransaction()
                            .remove(loginFragment)
                            .add(R.id.container_fragment_base_view, PostsFragment.newInstance(true), TAG_SUBSCRIBED_POSTS_FRAGMENT)
                            .commit();
                }
            } else {
                if (subscribedPostsFragment == null) { // if neither subscribedPostsFragment nor loginFragment exist
                    fm.beginTransaction()
                            .add(R.id.container_fragment_base_view, PostsFragment.newInstance(true), TAG_SUBSCRIBED_POSTS_FRAGMENT)
                            .commit();
                } else { // if only subscribedPostsFragment exists
                    //do nothing
                }
            }
        } else { // if not logged in
            if (loginFragment != null) {
                if (subscribedPostsFragment != null) { // if both subscribedPostsFragment and loginFragment exist
                    fm.beginTransaction()
                            .remove(subscribedPostsFragment)
                            .commit();
                } else { // if only loginFragment exists
                    //do nothing
                }
            } else {
                if (subscribedPostsFragment == null) { // if neither subscribedPostsFragment nor loginFragment exist
                    fm.beginTransaction()
                            .add(R.id.container_fragment_base_view, LoginFragment.newInstance(), TAG_LOGIN_FRAGMENT)
                            .commit();
                } else { // if only subscribedPostsFragment exists
                    fm.beginTransaction()
                            .remove(subscribedPostsFragment)
                            .add(R.id.container_fragment_base_view, LoginFragment.newInstance(), TAG_LOGIN_FRAGMENT)
                            .commit();
                }
            }
        }
    }
}
