package com.aaronhalbert.nosurfforreddit.fragments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;
import com.aaronhalbert.nosurfforreddit.databinding.FragmentPostBinding;

import javax.inject.Inject;
import javax.inject.Named;

abstract public class PostFragment extends BaseFragment {
    static final String KEY_POSITION = "position";
    static final String KEY_EXTERNAL_BROWSER = "externalBrowser";
    static final String KEY_IS_SUBSCRIBED_POST = "isSubscribedPost";

    final TextView[] comments = new TextView[3];
    final TextView[] commentsDetails = new TextView[3];
    final View[] dividers = new View[2];

    public int position;
    boolean externalBrowser;
    boolean isSubscribedPost;

    LiveData<PostsViewState> postsLiveDataViewState;
    OnFragmentInteractionListener mListener;
    FragmentPostBinding fragmentPostBinding;
    NoSurfViewModel viewModel;

    @Inject @Named("defaultSharedPrefs") SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        externalBrowser = preferences.getBoolean(KEY_EXTERNAL_BROWSER, false);
        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);

        // TODO: checking if savedInstanceState is null instead of using getArguments() crashes the app...why?
        if (getArguments() != null) {
            position = getArguments().getInt(KEY_POSITION);
            isSubscribedPost = getArguments().getBoolean(KEY_IS_SUBSCRIBED_POST);
        }

        if (isSubscribedPost) {
            postsLiveDataViewState = viewModel.getSubscribedPostsLiveDataViewState();
        } else {
            postsLiveDataViewState = viewModel.getAllPostsLiveDataViewState();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setupBinding(inflater, container);
        findPostViews();
        setupPostViews();
        observeCommentsFinishedLoadingLiveEvent();

        return fragmentPostBinding.getRoot();
    }

    public void onImageClick(View view) {
        if (externalBrowser) {
            if (mListener != null) {
                Uri uri = Uri.parse(postsLiveDataViewState.getValue().postData.get(position).url);
                mListener.launchExternalBrowser(uri);
            }
        } else {
            if (mListener != null) {
                mListener.launchWebView(postsLiveDataViewState.getValue().postData.get(position).url, null, false);
            }
        }
    }
    
    public LiveData<PostsViewState> getPostsLiveDataViewState() {
        return postsLiveDataViewState;
    }

    // region helper methods -----------------------------------------------------------------------

    private void setupBinding(LayoutInflater inflater, ViewGroup container) {
        fragmentPostBinding = FragmentPostBinding.inflate(getActivity().getLayoutInflater(),
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
        //display the appropriate text fields and dividers depending on how many comments the current post has
        viewModel.getCommentsFinishedLoadingLiveEvent().observe(this, aBoolean -> {
            if (aBoolean) {
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
        });
    }

    // endregion helper methods --------------------------------------------------------------------

    // region abstract methods ---------------------------------------------------------------------

    abstract void setupPostViews();

    // endregion abstract methods ------------------------------------------------------------------

    // region interfaces ---------------------------------------------------------------------------

    public interface OnFragmentInteractionListener {
        void launchWebView(String url, String tag, boolean doAnimation);
        void launchExternalBrowser(Uri uri);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // endregion interfaces ------------------------------------------------------------------------
}