package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.databinding.FragmentPostBinding;
import com.aaronhalbert.nosurfforreddit.repository.SettingsStore;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.PostFragmentViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory;
import com.aaronhalbert.nosurfforreddit.viewstate.LastClickedPostMetadata;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

/* base fragment for the detail view of a single post, when a row in the RecyclerView is clicked
 *
 * PostsFragment contains the master view */

abstract public class PostFragment extends BaseFragment {
    private static final String KEY_COMMENTS_ALREADY_LOADED = "commentsAlreadyLoaded";
    private static final String ZERO = "zero";

    private TextView[] comments;
    private TextView[] commentsDetails;
    private View[] dividers;

    @SuppressWarnings("WeakerAccess") @Inject ViewModelFactory viewModelFactory;
    @Inject SettingsStore settingsStore;

    private PostFragmentViewModel viewModel;
    private MainActivityViewModel mainActivityViewModel;
    LiveData<PostsViewState> postsViewStateLiveData;
    FragmentPostBinding fragmentPostBinding;
    @SuppressWarnings("WeakerAccess") public int lastClickedPostPosition;
    private String lastClickedPostId;
    private boolean commentsAlreadyLoaded;
    boolean externalBrowser;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(PostFragmentViewModel.class);
        mainActivityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        externalBrowser = settingsStore.isUseExternalBrowser();

        setHasOptionsMenu(true);
        lookupPostMetadata();

        /* avoid additional comments network call on config change */
        if (savedInstanceState != null) {
            commentsAlreadyLoaded = savedInstanceState
                    .getBoolean(KEY_COMMENTS_ALREADY_LOADED, false);
        }
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

        // avoid leaking any of these views when fragment goes on backstack
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

    // region listeners ----------------------------------------------------------------------------

    public void onImageClick(View view) {
        String url = mainActivityViewModel.getLastClickedPostMetadata().lastClickedPostUrl;

        launchLink(view, url);
    }

    // endregion listeners -------------------------------------------------------------------------

    // region getter methods -----------------------------------------------------------------------

    // expose these for the data binding class
    public LiveData<PostsViewState> getPostsViewStateLiveData() {
        return postsViewStateLiveData;
    }

    public PostFragmentViewModel getViewModel() {
        return viewModel;
    }

    // endregion getter methods --------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void setupBinding(ViewGroup container) {
        fragmentPostBinding = FragmentPostBinding.inflate(requireActivity().getLayoutInflater(),
                container,
                false);
        fragmentPostBinding.setPostFragment(this);
        fragmentPostBinding.setLifecycleOwner(this);
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

    private void observeCommentsFinishedLoadingLiveEvent() {
        viewModel.getCommentsViewStateLiveData().observe(getViewLifecycleOwner(), commentsViewState -> {
            if (lastClickedPostId.equals(commentsViewState.id) || (ZERO.equals(commentsViewState.id))) {
                updateCommentViewVisibilities();

                commentsAlreadyLoaded = true;
            }
        });
    }

    private void setupComments() {
        /* To get the comments for a given post, we have to take the post's ID and make a separate
         * API call, which happens here. */

        if (!commentsAlreadyLoaded) {
            observeCommentsFinishedLoadingLiveEvent();
            viewModel.fetchPostCommentsASync(lastClickedPostId);
        } else {
            updateCommentViewVisibilities();
        }
    }

    private void updateCommentViewVisibilities() {
        /* show the correct comment and divider views based on how many comments the post has */

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

    private void lookupPostMetadata() {
        LastClickedPostMetadata lastClickedPostMetadata = mainActivityViewModel.getLastClickedPostMetadata();

        lastClickedPostPosition = lastClickedPostMetadata.lastClickedPostPosition;
        lastClickedPostId = lastClickedPostMetadata.lastClickedPostId;

        if (lastClickedPostMetadata.lastClickedPostIsSubscribed) {
            postsViewStateLiveData = viewModel.getSubscribedPostsViewStateLiveData();
        } else {
            postsViewStateLiveData = viewModel.getAllPostsViewStateLiveData();
        }
    }

    // endregion helper methods --------------------------------------------------------------------

    // region abstract methods ---------------------------------------------------------------------

    abstract void setupPostViews();
    abstract void launchLink(View view, String url);

    // endregion abstract methods ------------------------------------------------------------------
}
