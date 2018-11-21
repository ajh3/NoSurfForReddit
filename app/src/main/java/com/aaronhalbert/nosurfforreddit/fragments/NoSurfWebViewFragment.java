package com.aaronhalbert.nosurfforreddit.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.webview.NoSurfWebViewClient;

import javax.inject.Inject;

public class NoSurfWebViewFragment extends BaseFragment {

    @SuppressWarnings("WeakerAccess") @Inject NoSurfWebViewClient noSurfWebViewClient;
    private String url;

    public static NoSurfWebViewFragment newInstance() {
        return new NoSurfWebViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);

        url = NoSurfWebViewFragmentArgs.fromBundle(getArguments()).getUrl();
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
