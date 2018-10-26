package com.aaronhalbert.nosurfforreddit;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.aaronhalbert.nosurfforreddit.fragments.BaseFragment;

import androidx.fragment.app.FragmentActivity;

public class NoSurfWebViewClient extends WebViewClient {

    private static final String NOSURF_REDIRECT_URI = "nosurfforreddit://oauth";

    private FragmentActivity hostFragmentActivity;

    public NoSurfWebViewClient(FragmentActivity hostFragmentActivity) {
        this.hostFragmentActivity = hostFragmentActivity;
    }

    @Override
    @TargetApi(21) //TODO: support down to 19
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();

        //checks if URL is a custom NoSurf redirect URI

        if (url.contains(NOSURF_REDIRECT_URI)) {
            Log.e(getClass().toString(), "URL is NoSurf redirect URI" + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            hostFragmentActivity.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }
}
