package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;
import android.view.View;

public class LinkPostFragment extends PostFragment {
    public static LinkPostFragment newInstance(int position, boolean isSubscribedPost, String id) {
        LinkPostFragment fragment = new LinkPostFragment();
        //TODO: store this state in VM instead of passing to fragment and storing in bundle?
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        args.putBoolean(KEY_IS_SUBSCRIBED_POST, isSubscribedPost);
        args.putString(KEY_ID, id);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    void setupPostViews() {
        fragmentPostBinding.postFragmentImageForLinkPostsOnly.setVisibility(View.VISIBLE);
    }
}
