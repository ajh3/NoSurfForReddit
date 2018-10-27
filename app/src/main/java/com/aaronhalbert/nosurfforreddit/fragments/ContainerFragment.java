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
import com.aaronhalbert.nosurfforreddit.ViewModelFactory;

import javax.inject.Inject;

public class ContainerFragment extends BaseFragment {
    private static final String TAG_SUBSCRIBED_POSTS_FRAGMENT = "subscribedPostsFragment";
    private static final String TAG_LOGIN_FRAGMENT = "loginFragment";

    @Inject ViewModelFactory viewModelFactory;
    private NoSurfViewModel viewModel;
    private FragmentManager fm;

    public static ContainerFragment newInstance() {
        ContainerFragment fragment = new ContainerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);

        fm = getChildFragmentManager();
        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);

        viewModel.getUserOAuthRefreshTokenLiveData().observe(this, s -> {
            //TODO: handle below logic inside the vm itself?
            //TODO: universalize login status inside vm?
            boolean isUserLoggedIn = !(s == null) && !(s.equals(""));
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

    //TODO: how do I simplify this total mess?
    public void setContainerChildFragment(boolean isUserLoggedIn) {
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
                            .add(R.id.container_fragment_base_view, SubscribedPostsFragment.newInstance(), TAG_SUBSCRIBED_POSTS_FRAGMENT)
                            .commit();
                }
            } else {
                if (subscribedPostsFragment == null) { // if neither subscribedPostsFragment nor loginFragment exist
                    fm.beginTransaction()
                            .add(R.id.container_fragment_base_view, SubscribedPostsFragment.newInstance(), TAG_SUBSCRIBED_POSTS_FRAGMENT)
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
