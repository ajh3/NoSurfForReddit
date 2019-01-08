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

package com.aaronhalbert.nosurfforreddit.ui.master;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.BaseFragment;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.ui.login.LoginFragment;
import com.aaronhalbert.nosurfforreddit.ui.main.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.utils.ViewModelFactory;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

/* This fragment is necessary only because it's difficult to directly swap pages inside a
 * FragmentPagerAdapter.
 *
 * When the user is logged out, it displays a LoginFragment to
 * prompt the user to log in, and when the user is logged in, it displays the user's
 * subscribed posts. */

public class ContainerFragment extends BaseFragment {
    private static final String TAG_SUBSCRIBED_POSTS_FRAGMENT = "subscribedPostsFragment";
    private static final String TAG_LOGIN_FRAGMENT = "loginFragment";

    @SuppressWarnings("WeakerAccess") @Inject ViewModelFactory viewModelFactory;
    private MainActivityViewModel viewModel;
    private FragmentManager fm;

    public static ContainerFragment newInstance() {
        return new ContainerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainActivityViewModel.class);
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
}
