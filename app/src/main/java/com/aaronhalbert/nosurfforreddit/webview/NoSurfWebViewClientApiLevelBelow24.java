package com.aaronhalbert.nosurfforreddit.webview;

import android.webkit.WebView;

import androidx.fragment.app.FragmentActivity;

public class NoSurfWebViewClientApiLevelBelow24 extends NoSurfWebViewClient {
    public NoSurfWebViewClientApiLevelBelow24(FragmentActivity hostFragmentActivity) {
        super(hostFragmentActivity);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        return processUrl(url);
    }
}
