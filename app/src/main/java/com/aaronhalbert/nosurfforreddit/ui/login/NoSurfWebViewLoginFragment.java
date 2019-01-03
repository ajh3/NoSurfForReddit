package com.aaronhalbert.nosurfforreddit.ui.login;

import com.aaronhalbert.nosurfforreddit.ui.detail.NoSurfWebViewFragment;

public class NoSurfWebViewLoginFragment extends NoSurfWebViewFragment {

    /* don't display a share button when WebView is being used to display the Reddit login page */
    @Override
    public void setupMenu() {
        setHasOptionsMenu(false);
    }
}
