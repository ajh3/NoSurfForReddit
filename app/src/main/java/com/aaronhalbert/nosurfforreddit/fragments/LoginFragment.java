package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aaronhalbert.nosurfforreddit.viewmodel.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.R;

import androidx.lifecycle.ViewModelProviders;

public class LoginFragment extends BaseFragment {
    private NoSurfViewModel viewModel;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(requireActivity()).get(NoSurfViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        Button loginButton = v.findViewById(R.id.login_fragment_button);
        loginButton.setOnClickListener(v1 -> login());

        return v;
    }

    private void login() {
        viewModel.setLoginFragmentClickEventsLiveData(true);
    }
}
