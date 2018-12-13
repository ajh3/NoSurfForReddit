package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;

/* for the first page of the RecyclerView - posts from r/all
 *
 * search for "AllPosts" globally in this project for more comments about AllPosts
 * vs. SubscribedPosts
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

    /* onRefresh is called directly when the user swipes to refresh. It is also called indirectly
     * when the user clicks "Refresh" in the menu, via PostsFragment.refreshWithAnimation()
     * triggering onRefresh() by posting a Runnable and turning on the animation manually. */
    @Override
    public void onRefresh() {
        viewModel.fetchAllPostsASync();
    }
}
