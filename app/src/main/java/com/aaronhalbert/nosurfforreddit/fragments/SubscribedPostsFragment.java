package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;

public class SubscribedPostsFragment extends PostsFragment {

    public static SubscribedPostsFragment newInstance() {
        return new SubscribedPostsFragment();
    }

    @Override
    PostsAdapter setPostsAdapter() {
        return new PostsAdapter(
                viewModel,
                this,
                true);
    }

    @Override
    void setPostsViewStateLiveData() {
        postsViewStateLiveData = viewModel.getSubscribedPostsViewStateLiveData();
    }

    @Override
    public void onRefresh() {
        viewModel.fetchSubscribedPostsASync();
    }
}
