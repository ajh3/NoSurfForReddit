package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;

public class AllPostsFragment extends PostsFragment {
    public static AllPostsFragment newInstance() {
        return new AllPostsFragment();
    }

    @Override
    PostsAdapter setPostsAdapter() {
        return new PostsAdapter(
                viewModel,
                this,
                false);
    }

    @Override
    void setPostsViewStateLiveData() {
        postsViewStateLiveData = viewModel.getAllPostsViewStateLiveData();
    }

    @Override
    public void onRefresh() {
        viewModel.fetchAllPostsASync();
    }
}
