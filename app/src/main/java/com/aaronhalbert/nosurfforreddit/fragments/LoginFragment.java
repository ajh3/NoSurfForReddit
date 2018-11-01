package com.aaronhalbert.nosurfforreddit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aaronhalbert.nosurfforreddit.R;

public class LoginFragment extends BaseFragment {
    private LoginFragmentInteractionListener loginFragmentInteractionListener;

    public static LoginFragment newInstance() {
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

    private void login() {
        if (loginFragmentInteractionListener != null) {
            loginFragmentInteractionListener.login();
        }
    }

    // region interfaces ---------------------------------------------------------------------------

    public interface LoginFragmentInteractionListener {
        void login();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoginFragmentInteractionListener) {
            loginFragmentInteractionListener = (LoginFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LoginFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginFragmentInteractionListener = null;
    }

    // endregion interfaces ------------------------------------------------------------------------
}
