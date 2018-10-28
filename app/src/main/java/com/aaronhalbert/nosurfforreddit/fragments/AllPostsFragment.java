package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;

public class AllPostsFragment extends PostsFragment {

    public static AllPostsFragment newInstance() {
        return new AllPostsFragment();
    }

    @Override
    PostsAdapter newPostsAdapter() {
        return new PostsAdapter((PostsAdapter.launchPostCallback) getActivity(),
                viewModel,
                this,
                false);
    }

    @Override
    void setPostsLiveData() {
        postsLiveData = viewModel.getAllPostsLiveData();
    }

    @Override
    public void onRefresh() {
        viewModel.fetchAllPostsSync();
    }
}
