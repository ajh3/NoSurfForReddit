package com.aaronhalbert.nosurfforreddit.fragments;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.aaronhalbert.nosurfforreddit.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExternalPostFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExternalPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExternalPostFragment extends Fragment {
    private static final String KEY_URL = "url";

    private String url;

    private WebView browser;

    private OnFragmentInteractionListener mListener;

    public ExternalPostFragment() {
        // Required empty public constructor
    }

    public static ExternalPostFragment newInstance(String url) {
        ExternalPostFragment fragment = new ExternalPostFragment();
        Bundle args = new Bundle();
        args.putString(KEY_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            url = getArguments().getString(KEY_URL);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_external_post, container, false);

        browser = result.findViewById(R.id.external_post_fragment_webview);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setBuiltInZoomControls(true);
        browser.getSettings().setDisplayZoomControls(false);
        browser.getSettings().setLoadWithOverviewMode(true);
        browser.getSettings().setUseWideViewPort(true);
        browser.loadUrl(url);
        
        return result;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
/*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
*/
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
