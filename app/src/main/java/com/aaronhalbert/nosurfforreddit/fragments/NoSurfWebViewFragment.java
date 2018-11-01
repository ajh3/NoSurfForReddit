package com.aaronhalbert.nosurfforreddit.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.aaronhalbert.nosurfforreddit.NoSurfWebViewClient;
import com.aaronhalbert.nosurfforreddit.R;

import javax.inject.Inject;

public class NoSurfWebViewFragment extends BaseFragment {
    private static final String KEY_URL = "url";

    @Inject NoSurfWebViewClient noSurfWebViewClient;

    private String url;

    public static NoSurfWebViewFragment newInstance(String url) {
        NoSurfWebViewFragment fragment = new NoSurfWebViewFragment();
        Bundle args = new Bundle();
        args.putString(KEY_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
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

        WebView browser = result.findViewById(R.id.nosurf_webview_fragment_webview);
        WebSettings browserSettings = browser.getSettings();
        browserSettings.setDomStorageEnabled(true); // Imgur needs this
        browserSettings.setJavaScriptEnabled(true);
        browserSettings.setBuiltInZoomControls(true);
        browserSettings.setDisplayZoomControls(false);
        browserSettings.setLoadWithOverviewMode(true);
        browserSettings.setUseWideViewPort(true);
        browser.setWebViewClient(noSurfWebViewClient);
        browser.loadUrl(url);
        
        return result;
    }
}
