package com.aaronhalbert.nosurfforreddit.fragments;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.viewstate.LastClickedPostMetadata;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;
import com.aaronhalbert.nosurfforreddit.databinding.FragmentPostBinding;

import javax.inject.Inject;
import javax.inject.Named;

abstract public class PostFragment extends BaseFragment {
    private static final String KEY_EXTERNAL_BROWSER = "externalBrowser";
    private static final String KEY_COMMENTS_ALREADY_LOADED = "commentsAlreadyLoaded";

    private final TextView[] comments = new TextView[3];
    private final TextView[] commentsDetails = new TextView[3];
    private final View[] dividers = new View[2];

    @SuppressWarnings("WeakerAccess")
    public int lastClickedPostPosition;
    private String lastClickedPostId;
    private boolean externalBrowser;
    private boolean commentsAlreadyLoaded;
    private PostFragmentInteractionListener postFragmentInteractionListener;
    private NoSurfViewModel viewModel;
    LiveData<PostsViewState> postsLiveDataViewState;
    FragmentPostBinding fragmentPostBinding;

    @SuppressWarnings("WeakerAccess")
    @Inject @Named("defaultSharedPrefs") SharedPreferences preferences;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(requireActivity()).get(NoSurfViewModel.class);

        setHasOptionsMenu(true);
        loadPrefs();
        lookupPostMetadata();

        // avoid additional comments network call on config change
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(KEY_COMMENTS_ALREADY_LOADED, commentsAlreadyLoaded);

        super.onSaveInstanceState(outState);
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region listeners ----------------------------------------------------------------------------

    public void onImageClick(View view) {
        String url = postsLiveDataViewState.getValue().postData.get(lastClickedPostPosition).url;

        if (postFragmentInteractionListener != null) {
            if (externalBrowser) {
                postFragmentInteractionListener.launchExternalBrowser(Uri.parse(url));
            } else {
                postFragmentInteractionListener.launchWebView(url, null, false);
            }
        }
    }

    // endregion listeners -------------------------------------------------------------------------

    // region getter methods -----------------------------------------------------------------------

    // for data binding class
    public LiveData<PostsViewState> getPostsLiveDataViewState() {
        return postsLiveDataViewState;
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

        viewModel.getCommentsFinishedLoadingLiveEvents().observe(this, aBoolean -> {

            if (aBoolean) {
                if (viewModel.getCommentsLiveDataViewState().getValue() != null) {

                    updateViewVisibilities();

                    viewModel.consumeCommentsLiveDataChangedEvent();
                    commentsAlreadyLoaded = true;
                }
            }
        });
    }

    private void setupComments() {
        if (!commentsAlreadyLoaded) {
            observeCommentsFinishedLoadingLiveEvent();
            viewModel.fetchPostCommentsSync(lastClickedPostId);
            viewModel.insertClickedPostId(lastClickedPostId);
        } else {
            updateViewVisibilities();
        }
    }

    private void updateViewVisibilities() {
        int numComments = viewModel.getCommentsLiveDataViewState().getValue().numComments;

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
            postsLiveDataViewState = viewModel.getStitchedSubscribedPostsLiveDataViewState();
        } else {
            postsLiveDataViewState = viewModel.getStitchedAllPostsLiveDataViewState();
        }
    }

    private void loadPrefs() {
        externalBrowser = preferences.getBoolean(KEY_EXTERNAL_BROWSER, false);
    }

    // endregion helper methods --------------------------------------------------------------------

    // region abstract methods ---------------------------------------------------------------------

    abstract void setupPostViews();

    // endregion abstract methods ------------------------------------------------------------------

    // region interfaces ---------------------------------------------------------------------------

    public interface PostFragmentInteractionListener {
        void launchWebView(String url, String tag, boolean doAnimation);
        void launchExternalBrowser(Uri uri);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PostFragmentInteractionListener) {
            postFragmentInteractionListener = (PostFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PostFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        postFragmentInteractionListener = null;
    }

    // endregion interfaces ------------------------------------------------------------------------
}
