package com.aaronhalbert.nosurfforreddit.fragments;

import android.view.View;

/* for displaying a Reddit link-type post, as opposed to a self-type
 *
 * also see SelfPostFragment */

public class LinkPostFragment extends PostFragment {
    public static LinkPostFragment newInstance() {
        return new LinkPostFragment();
    }

    @Override
    void setupPostViews() {
        fragmentPostBinding
                .postFragmentImageForLinkPostsOnly
                .setVisibility(View.VISIBLE);
    }
}
