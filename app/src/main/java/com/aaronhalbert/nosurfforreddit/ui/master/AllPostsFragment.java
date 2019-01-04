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
