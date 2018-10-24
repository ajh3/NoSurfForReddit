package com.aaronhalbert.nosurfforreddit;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.aaronhalbert.nosurfforreddit.fragments.BaseFragment;

import androidx.fragment.app.FragmentActivity;

public class NoSurfWebViewClient extends WebViewClient {

    private FragmentActivity hostFragmentActivity;

    public NoSurfWebViewClient(FragmentActivity hostFragmentActivity) {
        this.hostFragmentActivity = hostFragmentActivity;
    }

    @Override
    @TargetApi(21) //TODO: support down to 19
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();

        //TODO: document the purpose of this
        if (URLUtil.isNetworkUrl(url)) {
            return false;
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            hostFragmentActivity.startActivity(intent);
            return true;
        }
    }
}
