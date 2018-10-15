package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;

public class AllPostsFragment extends PostsFragment {

    public static AllPostsFragment newInstance() {
        AllPostsFragment fragment = new AllPostsFragment();
        return fragment;
    }

    @Override
    PostsAdapter newPostsAdapter() {
        PostsAdapter postsAdapter = new PostsAdapter((PostsAdapter.launchPostCallback) getActivity(),
                viewModel,
                this,
                false);
        return postsAdapter;
    }

    @Override
    void setPostsLiveData() {
        postsLiveData = viewModel.getAllPostsLiveData();
    }

    @Override
    public void onRefresh() {
        viewModel.refreshAllPosts();
    }
}
