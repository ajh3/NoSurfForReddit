package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;

public class SubscribedPostsFragment extends PostsFragment {

    public static SubscribedPostsFragment newInstance() {
        return new SubscribedPostsFragment();
    }

    @Override
    PostsAdapter setPostsAdapter() {
        return new PostsAdapter(
                postsFragmentInteractionListener,
                viewModel,
                this,
                true);
    }

    @Override
    void setPostsLiveDataViewState() {
        postsLiveDataViewState = viewModel.getMergedSubscribedPostsLiveDataViewState();
    }

    @Override
    public void onRefresh() {
        viewModel.fetchSubscribedPostsASync();
    }
}
