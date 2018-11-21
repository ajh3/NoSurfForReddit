package com.aaronhalbert.nosurfforreddit.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.activities.MainActivity;
import com.aaronhalbert.nosurfforreddit.network.NoSurfAuthenticator;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;

import androidx.lifecycle.ViewModelProviders;

public class LoginFragment extends BaseFragment {
    private MainActivityViewModel viewModel;
    private Activity activity;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        //TODO: make this fragment independent of its activity
        activity = getActivity();
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
        ((MainActivity) activity)
                .openLink(NoSurfAuthenticator.buildAuthUrl(), true);
    }
}
