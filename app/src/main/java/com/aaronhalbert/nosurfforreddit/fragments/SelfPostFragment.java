package com.aaronhalbert.nosurfforreddit.fragments;

import android.view.View;

public class SelfPostFragment extends PostFragment {
    public static SelfPostFragment newInstance() {
        return new SelfPostFragment();
    }

    @Override
    void setupPostViews() {
        fragmentPostBinding.postFragmentDividerUnderDetailsForSelfPostsOnly.setVisibility(View.VISIBLE);

        if (!(postsLiveDataViewState.getValue().postData.get(lastClickedPostPosition).selfTextHtml).equals("")) {
            fragmentPostBinding.postFragmentSelftextForSelfPostsOnly.setVisibility(View.VISIBLE);
            fragmentPostBinding.postFragmentDividerUnderSelftextForSelfPostsOnly.setVisibility(View.VISIBLE);
        }
    }
}
