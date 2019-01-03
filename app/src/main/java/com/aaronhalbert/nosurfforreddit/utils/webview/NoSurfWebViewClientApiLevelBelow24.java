package com.aaronhalbert.nosurfforreddit.utils.webview;

import android.webkit.WebView;

import androidx.fragment.app.FragmentActivity;

// also see NoSurfWebViewClient; Dagger injects correct version based on API level

public class NoSurfWebViewClientApiLevelBelow24 extends NoSurfWebViewClient {
    public NoSurfWebViewClientApiLevelBelow24(FragmentActivity hostFragmentActivity) {
        super(hostFragmentActivity);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        return processUrl(url);
    }
}
