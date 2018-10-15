package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;

public class SubscribedPostsFragment extends PostsFragment {

    public static SubscribedPostsFragment newInstance() {
        SubscribedPostsFragment fragment = new SubscribedPostsFragment();
        return fragment;
    }

    @Override
    PostsAdapter newPostsAdapter() {
        PostsAdapter postsAdapter = new PostsAdapter((PostsAdapter.launchPostCallback) getActivity(),
                viewModel,
                this,
                true);
        return postsAdapter;
    }

    @Override
    void setPostsLiveData() {
        postsLiveData = viewModel.getSubscribedPostsLiveData();
    }

    @Override
    public void onRefresh() {
        viewModel.refreshSubscribedPosts();
    }
}
