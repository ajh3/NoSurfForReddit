package com.aaronhalbert.nosurfforreddit.fragments;

import android.view.View;

public class LinkPostFragment extends PostFragment {
    public static LinkPostFragment newInstance() {
        return new LinkPostFragment();
    }

    @Override
    void setupPostViews() {
        fragmentPostBinding.postFragmentImageForLinkPostsOnly.setVisibility(View.VISIBLE);
    }
}
