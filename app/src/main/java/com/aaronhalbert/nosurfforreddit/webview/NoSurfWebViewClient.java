package com.aaronhalbert.nosurfforreddit.webview;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.FragmentActivity;

// also see NoSurfWebViewClientApiLevelBelow24; Dagger injects correct version based on API level

public class NoSurfWebViewClient extends WebViewClient {
    private static final String NOSURF_REDIRECT_URI = "nosurfforreddit://oauth";
    private final FragmentActivity hostFragmentActivity;

    public NoSurfWebViewClient(FragmentActivity hostFragmentActivity) {
        this.hostFragmentActivity = hostFragmentActivity;
    }

    // hook into link clicks to check if the activity should capture a click as an intent
    // currently only used during user login
    @Override
    @TargetApi(24)
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();

        return processUrl(url);
    }

    //checks if URL is a custom NoSurf redirect URI
    boolean processUrl(String url) {
        if (url.contains(NOSURF_REDIRECT_URI)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            hostFragmentActivity.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }
}
