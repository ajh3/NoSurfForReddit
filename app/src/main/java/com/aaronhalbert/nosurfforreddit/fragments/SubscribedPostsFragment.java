package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;

public class SubscribedPostsFragment extends PostsFragment {

    public static PostsFragment newInstance() {
        PostsFragment fragment = new SubscribedPostsFragment();
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
        postsLiveData = viewModel.getHomePostsLiveData();
    }

    @Override
    public void onRefresh() {
        viewModel.getHomePostsLiveData();
    }
}
