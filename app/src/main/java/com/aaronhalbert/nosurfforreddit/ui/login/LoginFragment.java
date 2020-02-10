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

package com.aaronhalbert.nosurfforreddit.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aaronhalbert.nosurfforreddit.BaseFragment;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.data.remote.auth.AuthenticatorUtils;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.GotoLoginUrlGlobalAction;
import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.gotoLoginUrlGlobalAction;

public class LoginFragment extends BaseFragment {
    @Inject AuthenticatorUtils authenticatorUtils;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        Button loginButton = v.findViewById(R.id.login_fragment_button);
        loginButton.setOnClickListener(v1 -> login());

        return v;
    }

    /* onNewIntent in MainActivity captures the result of this login attempt, and is responsible
    * for calling viewModel.logUserIn() if it's successful */
    private void login() {
        GotoLoginUrlGlobalAction action
                = gotoLoginUrlGlobalAction(authenticatorUtils.buildAuthUrl());

        NavHostFragment.findNavController(this).navigate(action);
    }
}
