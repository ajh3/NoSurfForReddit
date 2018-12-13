package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aaronhalbert.nosurfforreddit.R;

import androidx.navigation.Navigation;

import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.GotoLoginUrlGlobalAction;
import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.gotoLoginUrlGlobalAction;
import static com.aaronhalbert.nosurfforreddit.repository.NoSurfAuthenticator.buildAuthUrl;

public class LoginFragment extends BaseFragment {

    static LoginFragment newInstance() {
        return new LoginFragment();
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
                = gotoLoginUrlGlobalAction(buildAuthUrl());

        Navigation.findNavController(getView()).navigate(action);
    }
}
