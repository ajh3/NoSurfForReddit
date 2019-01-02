package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.repository.AuthenticatorUtils;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.GotoLoginUrlGlobalAction;
import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.gotoLoginUrlGlobalAction;

public class LoginFragment extends BaseFragment {
    @Inject AuthenticatorUtils authenticatorUtils;

    static LoginFragment newInstance() {
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

        Navigation.findNavController(getView()).navigate(action);
    }
}
