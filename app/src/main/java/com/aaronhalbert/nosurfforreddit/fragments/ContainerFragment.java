package com.aaronhalbert.nosurfforreddit.fragments;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
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
        return new ContainerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(requireActivity()).get(NoSurfViewModel.class);
        fm = getChildFragmentManager();

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

    private void observeIsUserLoggedInLiveData() {
        viewModel.getIsUserLoggedInLiveData().observe(this,
                isUserLoggedIn -> refreshContainerChildFragment(isUserLoggedIn));
    }
    
    private void refreshContainerChildFragment(boolean isUserLoggedIn) {
        Log.e(getClass().toString(), "isUserLoggedIn: " + isUserLoggedIn);
        FragmentTransaction ft = fm.beginTransaction();

        if (isUserLoggedIn) {
            ft.hide(findLoginFragment()).show(findSubscribedPostsFragment()).commit();
        } else {
            ft.show(findLoginFragment()).hide(findSubscribedPostsFragment()).commit();
        }
    }

    private Fragment findLoginFragment() {
        return fm.findFragmentByTag(TAG_LOGIN_FRAGMENT);
    }

    private Fragment findSubscribedPostsFragment() {
        return fm.findFragmentByTag(TAG_SUBSCRIBED_POSTS_FRAGMENT);
    }
}
