package com.aaronhalbert.nosurfforreddit.fragments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.webview.LaunchWebViewParams;
import com.aaronhalbert.nosurfforreddit.viewmodel.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.viewstate.LastClickedPostMetadata;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;
import com.aaronhalbert.nosurfforreddit.databinding.FragmentPostBinding;

abstract public class PostFragment extends BaseFragment {
    private static final String KEY_COMMENTS_ALREADY_LOADED = "commentsAlreadyLoaded";

    private final TextView[] comments = new TextView[3];
    private final TextView[] commentsDetails = new TextView[3];
    private final View[] dividers = new View[2];

    @SuppressWarnings("WeakerAccess")
    public int lastClickedPostPosition;
    private String lastClickedPostId;
    private boolean commentsAlreadyLoaded;
    private NoSurfViewModel viewModel;
    LiveData<PostsViewState> postsViewStateLiveData;
    FragmentPostBinding fragmentPostBinding;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(requireActivity()).get(NoSurfViewModel.class);

        setHasOptionsMenu(true);
        lookupPostMetadata();

        /* avoid additional comments network call on config change
         *
         * Could wrap the comments in an Event wrapper and handle it like navigation events,
         * but I'm not using saved instance state for much else, so let's use the Bundle here
         * just to mix things up */
        if (savedInstanceState != null) {
            commentsAlreadyLoaded = savedInstanceState.getBoolean(KEY_COMMENTS_ALREADY_LOADED, false);
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
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_COMMENTS_ALREADY_LOADED, commentsAlreadyLoaded);

        super.onSaveInstanceState(outState);
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region listeners ----------------------------------------------------------------------------

    public void onImageClick(View view) {
        String url = postsViewStateLiveData.getValue().postData.get(lastClickedPostPosition).url;

        viewModel.setPostFragmentClickEventsLiveData(new LaunchWebViewParams(url, null, false));
    }

    // endregion listeners -------------------------------------------------------------------------

    // region getter methods -----------------------------------------------------------------------

    // expose this for the data binding class
    public LiveData<PostsViewState> getPostsViewStateLiveData() {
        return postsViewStateLiveData;
    }

    // endregion getter methods --------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void setupBinding(ViewGroup container) {
        fragmentPostBinding = FragmentPostBinding.inflate(requireActivity().getLayoutInflater(),
                container,
                false);
        fragmentPostBinding.setViewModel(viewModel);
        fragmentPostBinding.setPostFragment(this);
        fragmentPostBinding.setLifecycleOwner(this);
    }

    private void findPostViews() {
        comments[0] = fragmentPostBinding.postFragmentFirstComment;
        comments[1] = fragmentPostBinding.postFragmentSecondComment;
        comments[2] = fragmentPostBinding.postFragmentThirdComment;

        comments[0].setMovementMethod(LinkMovementMethod.getInstance());
        comments[1].setMovementMethod(LinkMovementMethod.getInstance());
        comments[2].setMovementMethod(LinkMovementMethod.getInstance());

        commentsDetails[0] = fragmentPostBinding.postFragmentFirstCommentDetails;
        commentsDetails[1] = fragmentPostBinding.postFragmentSecondCommentDetails;
        commentsDetails[2] = fragmentPostBinding.postFragmentThirdCommentDetails;

        dividers[0] = fragmentPostBinding.postFragmentDividerUnderFirstComment;
        dividers[1] = fragmentPostBinding.postFragmentDividerUnderSecondComment;
    }

    private void observeCommentsFinishedLoadingLiveEvent() {
        viewModel.getCommentsViewStateLiveData().observe(this, commentsViewState -> {
            if (lastClickedPostId.equals(commentsViewState.id)) {
                updateViewVisibilities();

                commentsAlreadyLoaded = true;
            }
        });
    }

    private void setupComments() {
        if (!commentsAlreadyLoaded) {
            observeCommentsFinishedLoadingLiveEvent();
            viewModel.fetchPostCommentsASync(lastClickedPostId);
            viewModel.insertClickedPostId(lastClickedPostId);
        } else {
            updateViewVisibilities();
        }
    }

    private void updateViewVisibilities() {
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
        LastClickedPostMetadata lastClickedPostMetadata = viewModel.getLastClickedPostMetadata();

        lastClickedPostPosition = lastClickedPostMetadata.getLastClickedPostPosition();
        lastClickedPostId = lastClickedPostMetadata.getLastClickedPostId();

        if (lastClickedPostMetadata.isLastClickedPostIsSubscribed()) {
            postsViewStateLiveData = viewModel.getSubscribedPostsViewStateLiveData();
        } else {
            postsViewStateLiveData = viewModel.getAllPostsViewStateLiveData();
        }
    }

    // endregion helper methods --------------------------------------------------------------------

    // region abstract methods ---------------------------------------------------------------------

    abstract void setupPostViews();

    // endregion abstract methods ------------------------------------------------------------------
}
