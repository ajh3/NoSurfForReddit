package com.aaronhalbert.nosurfforreddit.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.ShareHelper;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.webview.NoSurfWebViewClient;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProviders;

public class NoSurfWebViewFragment extends BaseFragment {
    @SuppressWarnings("WeakerAccess") @Inject NoSurfWebViewClient noSurfWebViewClient;
    private MainActivityViewModel mainActivityViewModel;
    private String url;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);

        setupMenu();

        mainActivityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);

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

    // endregion lifecycle methods -----------------------------------------------------------------

    // region menu ---------------------------------------------------------------------------------

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_share:
                ShareHelper shareHelper = new ShareHelper(getContext());
                shareHelper.createShareIntent(mainActivityViewModel.getLastClickedPostDatum().permalink);
                shareHelper.launchShareIntent();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // endregion menu ------------------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    void setupMenu() {
        setHasOptionsMenu(true);
    }

    // endregion helper methods --------------------------------------------------------------------
}
