package com.aaronhalbert.nosurfforreddit.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.PostsViewState;
import com.aaronhalbert.nosurfforreddit.databinding.FragmentPostBinding;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

//TODO: build self post functionality into this class and eliminate SelfPostFragment
public class PostFragment extends Fragment {
    private static final String KEY_POSITION = "position";
    private static final String KEY_EXTERNAL_BROWSER = "externalBrowser";
    private static final String KEY_IS_SELF_POST = "isSelfPost";
    private static final String KEY_IS_SUBSCRIBED_POST = "isSubscribedPost";

    public int position;
    boolean externalBrowser;
    boolean isSelfPost;
    boolean isSubscribedPost;

    private LiveData<PostsViewState> postsViewStateLiveData;

    private OnFragmentInteractionListener mListener;

    SharedPreferences preferences;
    FragmentPostBinding fragmentPostBinding = null;
    NoSurfViewModel viewModel = null;

    public PostFragment() {
        // Required empty public constructor
    }

    public static PostFragment newInstance(int position, boolean isSelfPost, boolean isSubscribedPost) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        args.putBoolean(KEY_IS_SELF_POST, isSelfPost);
        args.putBoolean(KEY_IS_SUBSCRIBED_POST, isSubscribedPost);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(KEY_POSITION);
            isSelfPost = getArguments().getBoolean(KEY_IS_SELF_POST);
            isSubscribedPost = getArguments().getBoolean(KEY_IS_SUBSCRIBED_POST);
        }
        setHasOptionsMenu(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        externalBrowser = preferences.getBoolean(KEY_EXTERNAL_BROWSER, false);
        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);

        if (isSubscribedPost) {
            postsViewStateLiveData = viewModel.getHomePostsLiveDataViewState();
        } else {
            postsViewStateLiveData = viewModel.getAllPostsLiveDataViewState();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentPostBinding = FragmentPostBinding.inflate(getActivity().getLayoutInflater(), container, false);
        fragmentPostBinding.setViewModel(viewModel);
        fragmentPostBinding.setPostFragment(this);
        fragmentPostBinding.setLifecycleOwner(this); //comments Transformation in ViewModel won't be called without this!

        final TextView[] comments = new TextView[3];
        final TextView[] commentsDetails = new TextView[3];
        final View[] dividers = new View[2];

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

        if (isSelfPost) {
            fragmentPostBinding.postFragmentDividerUnderDetailsForSelfPostsOnly.setVisibility(View.VISIBLE);
            if (!(postsViewStateLiveData.getValue().postData.get(position).selfTextHtml).equals("")) {
                fragmentPostBinding.postFragmentSelftextForSelfPostsOnly.setVisibility(View.VISIBLE);
                fragmentPostBinding.postFragmentDividerUnderSelftextForSelfPostsOnly.setVisibility(View.VISIBLE);
            }
        } else {
            fragmentPostBinding.postFragmentImageForLinkPostsOnly.setVisibility(View.VISIBLE);
        }

        //display the appropriate text fields and dividers depending on how many comments the current post has
        viewModel.getCommentsFinishedLoadingLiveEvent().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
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
            }
        });

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void launchWebView(String url, String tag);
    }
}