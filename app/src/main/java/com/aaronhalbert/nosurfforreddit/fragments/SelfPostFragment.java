package com.aaronhalbert.nosurfforreddit.fragments;

import android.view.View;

/* for displaying a Reddit self-type post, as opposed to a link-type
 *
 * also see LinkPostFragment */

public class SelfPostFragment extends PostFragment {

    @Override
    void setupPostViews() {
        fragmentPostBinding
                .postFragmentDividerUnderDetailsForSelfPostsOnly
                .setVisibility(View.VISIBLE);

        if (!"".equals(postsViewStateLiveData.getValue().postData.get(lastClickedPostPosition)
                .selfTextHtml)) {

            fragmentPostBinding
                    .postFragmentSelftextForSelfPostsOnly
                    .setVisibility(View.VISIBLE);

            fragmentPostBinding
                    .postFragmentDividerUnderSelftextForSelfPostsOnly
                    .setVisibility(View.VISIBLE);
        }
    }

    @Override
    void launchLink(View view, String url) {
        // do nothing; only applies to LinkPostFragment
    }
}
