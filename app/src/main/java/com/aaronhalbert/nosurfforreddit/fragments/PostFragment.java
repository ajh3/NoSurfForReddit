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

abstract public class PostFragment extends Fragment {
    static final String KEY_POSITION = "position";
    private static final String KEY_EXTERNAL_BROWSER = "externalBrowser";
    static final String KEY_IS_SUBSCRIBED_POST = "isSubscribedPost";

    final TextView[] comments = new TextView[3];
    final TextView[] commentsDetails = new TextView[3];
    final View[] dividers = new View[2];

    public int position;
    boolean externalBrowser;
    boolean isSubscribedPost;

    LiveData<PostsViewState> postsViewStateLiveData;

    private OnFragmentInteractionListener mListener;

    SharedPreferences preferences;
    FragmentPostBinding fragmentPostBinding = null;
    NoSurfViewModel viewModel = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(KEY_POSITION);
            isSubscribedPost = getArguments().getBoolean(KEY_IS_SUBSCRIBED_POST);
        }
        setHasOptionsMenu(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        externalBrowser = preferences.getBoolean(KEY_EXTERNAL_BROWSER, false);
        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);

        if (isSubscribedPost) {
            postsViewStateLiveData = viewModel.getSubscribedPostsLiveDataViewState();
        } else {
            postsViewStateLiveData = viewModel.getAllPostsLiveDataViewState();
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

    //TODO: move this to MainActivity
    public void launchWebView() {
        if (mListener != null) {
            mListener.launchWebView(postsViewStateLiveData.getValue().postData.get(position).url, null);
        }
    }

    //TODO: move this to MainActivity
    public void launchExternalBrowser() {
        if (mListener != null) {
            //mListener.launchWebView(url, null);
            //TODO: pull this out into separate method
            Uri uri = Uri.parse(postsViewStateLiveData.getValue().postData.get(position).url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    public void onImageClick(View view) {
        if (externalBrowser) {
            launchExternalBrowser();
        } else {
            launchWebView();
        }
    }

    public LiveData<PostsViewState> getPostsViewStateLiveData() {
        return postsViewStateLiveData;
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

    public interface OnFragmentInteractionListener {
        void launchWebView(String url, String tag);
    }

    private void setupBinding(LayoutInflater inflater, ViewGroup container) {
        fragmentPostBinding = FragmentPostBinding.inflate(getActivity().getLayoutInflater(), container, false);
        fragmentPostBinding.setViewModel(viewModel);
        fragmentPostBinding.setPostFragment(this);
        fragmentPostBinding.setLifecycleOwner(this); //comments Transformation in ViewModel won't be called without this!
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
        });
    }

    abstract void setupPostViews();
}