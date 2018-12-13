package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;

/* for the second page of the RecyclerView - posts from the user's subscribed subreddits
 *
 * only used when user is logged in
 *
 * search for "SubscribedPosts" globally in this project for more comments about SubscribedPosts
 * vs. AllPosts
 *
 * also see AllPostsFragment*/

public class SubscribedPostsFragment extends PostsFragment {
    static SubscribedPostsFragment newInstance() {
        return new SubscribedPostsFragment();
    }

    @Override
    PostsAdapter createPostsAdapter() {
        return new PostsAdapter(
                viewModel,
                mainActivityViewModel,
                this,
                true);
    }

    @Override
    void selectPostsViewStateLiveData() {
        postsViewStateLiveData = viewModel.getSubscribedPostsViewStateLiveData();
    }

    /* see comments in AllPostsFragment.onRefresh() */
    @Override
    public void onRefresh() {
        viewModel.fetchSubscribedPostsASync();
    }
}
