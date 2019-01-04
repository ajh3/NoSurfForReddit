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

package com.aaronhalbert.nosurfforreddit.ui.detail;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.BaseFragment;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.data.settings.SettingsStore;
import com.aaronhalbert.nosurfforreddit.databinding.FragmentPostBinding;
import com.aaronhalbert.nosurfforreddit.ui.main.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.utils.SharePost;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProviders;

/* base fragment for the detail view of a single post, when a row in the RecyclerView is clicked
 *
 * PostsFragment is the master view */

abstract public class PostFragment extends BaseFragment {
    private static final String KEY_COMMENTS_ALREADY_LOADED = "commentsAlreadyLoaded";
    private static final String ZERO = "zero";

    @Inject SettingsStore settingsStore;
    @SuppressWarnings("WeakerAccess") public MainActivityViewModel viewModel;
    FragmentPostBinding fragmentPostBinding;

    private TextView[] comments;
    private TextView[] commentsDetails;
    private View[] dividers;

    private boolean commentsAlreadyLoaded;
    boolean externalBrowser;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        externalBrowser = settingsStore.isUseExternalBrowser();
        checkIfCommentsAlreadyLoaded(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setupBinding(container);
        findPostViews();
        setupPostViews();
        setupComments();

        return fragmentPostBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        /* avoid leaking any of these views when fragment goes on backstack
         *
         * see PostsFragment.onDestroyView() for a more detailed explanation of this leak */
        fragmentPostBinding = null;
        comments = null;
        commentsDetails = null;
        dividers = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_COMMENTS_ALREADY_LOADED, commentsAlreadyLoaded);
        super.onSaveInstanceState(outState);
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region menu ---------------------------------------------------------------------------------

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_share:
                SharePost sharePost = new SharePost(getContext());
                sharePost.createShareIntent(viewModel.getLastClickedPostDatum().permalink);
                sharePost.launchShareIntent();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // endregion menu ------------------------------------------------------------------------------

    // region listeners ----------------------------------------------------------------------------

    public void onImageClick(View view) {
        launchLink(view, viewModel.getLastClickedPostDatum().url);
    }

    // endregion listeners -------------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void setupBinding(ViewGroup container) {
        fragmentPostBinding = FragmentPostBinding.inflate(requireActivity().getLayoutInflater(),
                container,
                false);
        fragmentPostBinding.setPostFragment(this);
        fragmentPostBinding.setLifecycleOwner(getViewLifecycleOwner());
    }

    private void findPostViews() {
        comments = new TextView[3];
        commentsDetails = new TextView[3];
        dividers = new View[2];

        // put these views into arrays to more easily work with them in for loops
        comments[0] = fragmentPostBinding.postFragmentFirstComment;
        comments[1] = fragmentPostBinding.postFragmentSecondComment;
        comments[2] = fragmentPostBinding.postFragmentThirdComment;

        // setting a LinkMovementMethod on the comments fields makes links clickable
        MovementMethod m = LinkMovementMethod.getInstance();

        comments[0].setMovementMethod(m);
        comments[1].setMovementMethod(m);
        comments[2].setMovementMethod(m);

        commentsDetails[0] = fragmentPostBinding.postFragmentFirstCommentDetails;
        commentsDetails[1] = fragmentPostBinding.postFragmentSecondCommentDetails;
        commentsDetails[2] = fragmentPostBinding.postFragmentThirdCommentDetails;

        dividers[0] = fragmentPostBinding.postFragmentDividerUnderFirstComment;
        dividers[1] = fragmentPostBinding.postFragmentDividerUnderSecondComment;
    }

    private void setupComments() {
        /* To get the comments for a given post, we have to take the post's ID and make a separate
         * API call, which happens here. */

        if (!commentsAlreadyLoaded) {
            observeCommentsFinishedLoadingLiveEvent();
            viewModel.fetchPostCommentsASync(viewModel.getLastClickedPostDatum().id);
        } else {
            updateCommentViewVisibilities();
        }
    }

    private void observeCommentsFinishedLoadingLiveEvent() {
        viewModel.getCommentsViewStateLiveData().observe(getViewLifecycleOwner(), commentsViewState -> {
            if ((viewModel.getLastClickedPostDatum().id).equals(commentsViewState.id) || (ZERO.equals(commentsViewState.id))) {
                updateCommentViewVisibilities();
                commentsAlreadyLoaded = true;
            }
        });
    }

    /* show the correct comment and divider views based on how many comments the post has */
    private void updateCommentViewVisibilities() {
        int numComments = viewModel.getCommentsViewStateLiveData().getValue().numComments;

        for (int i = 0; i < numComments; i++) {
            comments[i].setVisibility(View.VISIBLE);
            commentsDetails[i].setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < (numComments - 1); i++) {
            dividers[i].setVisibility(View.VISIBLE);
        }

        fragmentPostBinding.postFragmentCommentProgressBar.setVisibility(View.GONE);
    }

    private void checkIfCommentsAlreadyLoaded(Bundle savedInstanceState) {
        /* avoid additional comments network call on config change */
        if (savedInstanceState != null) {
            commentsAlreadyLoaded = savedInstanceState
                    .getBoolean(KEY_COMMENTS_ALREADY_LOADED, false);
        }
    }

    // endregion helper methods --------------------------------------------------------------------

    // region abstract methods ---------------------------------------------------------------------

    abstract void setupPostViews();
    abstract void launchLink(View view, String url);

    // endregion abstract methods ------------------------------------------------------------------
}
