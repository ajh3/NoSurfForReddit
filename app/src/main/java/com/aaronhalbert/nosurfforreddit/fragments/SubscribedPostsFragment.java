package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;

public class SubscribedPostsFragment extends PostsFragment {

    public static SubscribedPostsFragment newInstance() {
        return new SubscribedPostsFragment();
    }

    @Override
    PostsAdapter newPostsAdapter() {
        return new PostsAdapter((PostsAdapter.launchPostCallback) getActivity(),
                viewModel,
                this,
                true);
    }

    @Override
    void setPostsLiveData() {
        postsLiveData = viewModel.getSubscribedPostsLiveData();
    }

    @Override
    public void onRefresh() {
        viewModel.fetchSubscribedPostsSync();
    }
}
