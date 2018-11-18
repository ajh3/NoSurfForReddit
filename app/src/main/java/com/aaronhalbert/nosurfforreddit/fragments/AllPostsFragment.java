package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;

/* for the first page of the RecyclerView - posts from r/all
 *
 * search for "subscribedposts" globally in this project for more comments about allposts
 * vs. subscribedposts
 *
 * also see SubscribedPostsFragment */

public class AllPostsFragment extends PostsFragment {
    public static AllPostsFragment newInstance() {
        return new AllPostsFragment();
    }

    @Override
    PostsAdapter createPostsAdapter() {
        return new PostsAdapter(
                viewModel,
                mainActivityViewModel,
                this,
                false);
    }

    @Override
    void selectPostsViewStateLiveData() {
        postsViewStateLiveData = viewModel.getAllPostsViewStateLiveData();
    }

    @Override
    public void onRefresh() {
        viewModel.fetchAllPostsASync();
    }
}
