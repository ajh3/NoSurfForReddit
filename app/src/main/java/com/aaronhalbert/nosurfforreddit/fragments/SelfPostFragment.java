package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;
import android.view.View;

public class SelfPostFragment extends PostFragment {
    public static SelfPostFragment newInstance(int position, boolean isSubscribedPost, String id) {
        SelfPostFragment fragment = new SelfPostFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        args.putBoolean(KEY_IS_SUBSCRIBED_POST, isSubscribedPost);
        args.putString(KEY_ID, id);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    void setupPostViews() {
        fragmentPostBinding.postFragmentDividerUnderDetailsForSelfPostsOnly.setVisibility(View.VISIBLE);

        if (!(postsLiveDataViewState.getValue().postData.get(position).selfTextHtml).equals("")) {
            fragmentPostBinding.postFragmentSelftextForSelfPostsOnly.setVisibility(View.VISIBLE);
            fragmentPostBinding.postFragmentDividerUnderSelftextForSelfPostsOnly.setVisibility(View.VISIBLE);
        }
    }
}
