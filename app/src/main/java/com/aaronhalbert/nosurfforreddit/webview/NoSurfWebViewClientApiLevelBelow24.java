package com.aaronhalbert.nosurfforreddit.webview;

import android.webkit.WebView;

import androidx.fragment.app.FragmentActivity;

// also see NoSurfWebViewClient; Dagger injects correct version based on API level

public class NoSurfWebViewClientApiLevelBelow24 extends NoSurfWebViewClient {
    public NoSurfWebViewClientApiLevelBelow24(FragmentActivity hostFragmentActivity) {
        super(hostFragmentActivity);
    }

    // hook into link clicks to check if the activity should capture a click as an intent
    // currently only used during user login
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        return processUrl(url);
    }
}
