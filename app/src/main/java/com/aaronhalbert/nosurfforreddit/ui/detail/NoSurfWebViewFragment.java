/*
 * Copyright (c) 2018-present, Aaron J. Halbert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.aaronhalbert.nosurfforreddit.ui.detail;

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

import com.aaronhalbert.nosurfforreddit.BaseFragment;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.ui.main.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.ui.utils.SharePost;
import com.aaronhalbert.nosurfforreddit.ui.utils.webview.NoSurfWebViewClient;

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
                SharePost sharePost = new SharePost(getContext());
                sharePost.createShareIntent(mainActivityViewModel.getLastClickedPostDatum().permalink);
                sharePost.launchShareIntent();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // endregion menu ------------------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    protected void setupMenu() {
        setHasOptionsMenu(true);
    }

    // endregion helper methods --------------------------------------------------------------------
}
