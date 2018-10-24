package com.aaronhalbert.nosurfforreddit.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.aaronhalbert.nosurfforreddit.R;

public class NoSurfWebViewFragment extends BaseFragment {
    private static final String KEY_URL = "url";

    private String url;

    private WebView browser;

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

        browser = result.findViewById(R.id.nosurf_webview_fragment_webview);
        WebSettings browserSettings = browser.getSettings();
        browserSettings.setDomStorageEnabled(true); // Imgur needs this
        browserSettings.setJavaScriptEnabled(true);
        browserSettings.setBuiltInZoomControls(true);
        browserSettings.setDisplayZoomControls(false);
        browserSettings.setLoadWithOverviewMode(true);
        browserSettings.setUseWideViewPort(true);
        browser.setWebViewClient(new WebViewClient() { // needed to load all user clicks inside the WebView

            @Override
            @TargetApi(21) //TODO: support down to 19
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (URLUtil.isNetworkUrl(url)) {
                    return false;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
            }
        });
        browser.loadUrl(url);
        
        return result;
    }
}
