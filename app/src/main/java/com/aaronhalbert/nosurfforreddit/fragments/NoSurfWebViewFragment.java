package com.aaronhalbert.nosurfforreddit.fragments;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.aaronhalbert.nosurfforreddit.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NoSurfWebViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NoSurfWebViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoSurfWebViewFragment extends Fragment {
    private static final String KEY_URL = "url";

    private String url;

    private WebView browser;

    public NoSurfWebViewFragment() {
        // Required empty public constructor
    }

    public static NoSurfWebViewFragment newInstance(String url) {
        NoSurfWebViewFragment fragment = new NoSurfWebViewFragment();
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
        View result = inflater.inflate(R.layout.fragment_nosurf_webview, container, false);
        Log.e(getClass().toString(), Boolean.toString(result.isHardwareAccelerated()));

        browser = result.findViewById(R.id.nosurf_webview_fragment_webview);
        WebSettings browserSettings = browser.getSettings();
        browserSettings.setDomStorageEnabled(true); // Imgur needs this
        browserSettings.setJavaScriptEnabled(true);
        browserSettings.setBuiltInZoomControls(true);
        browserSettings.setDisplayZoomControls(false);
        browserSettings.setLoadWithOverviewMode(true);
        browserSettings.setUseWideViewPort(true);
        browser.setWebViewClient(new WebViewClient()); // load all user clicks inside the WebView
        browser.loadUrl(url);
        
        return result;
    }
}
