package com.aaronhalbert.nosurfforreddit.fragments;

public class NoSurfWebViewLoginFragment extends NoSurfWebViewFragment {

    /* don't display a share button when WebView is being used to display the Reddit login page */
    @Override
    void setupMenu() {
        setHasOptionsMenu(false);
    }
}
