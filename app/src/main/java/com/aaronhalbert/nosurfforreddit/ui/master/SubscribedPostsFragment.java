/*
 * Copyright (c) 2018-present, Aaron J. Halbert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.aaronhalbert.nosurfforreddit.ui.master;

/* for the second page of the RecyclerView - posts from the user's subscribed subreddits
 *
 * only used when user is logged in
 *
 * search for "SubscribedPosts" globally in this project for more comments about SubscribedPosts
 * vs. AllPosts
 *
 * also see AllPostsFragment*/

@SuppressWarnings("WeakerAccess")
public class SubscribedPostsFragment extends PostsFragment {
    static SubscribedPostsFragment newInstance() {
        return new SubscribedPostsFragment();
    }

    @Override
    PostsAdapter createPostsAdapter() {
        return new PostsAdapter(
                viewModel,
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
