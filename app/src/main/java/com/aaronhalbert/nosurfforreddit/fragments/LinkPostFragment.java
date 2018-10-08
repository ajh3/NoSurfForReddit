package com.aaronhalbert.nosurfforreddit.fragments;

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

import com.aaronhalbert.nosurfforreddit.GlideApp;
import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.databinding.FragmentLinkPostBinding;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LinkPostFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LinkPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

//TODO: build self post functionality into this class and eliminate SelfPostFragment
public class LinkPostFragment extends Fragment {
    private static final String KEY_POSITION = "position";
    private static final String KEY_EXTERNAL_BROWSER = "externalBrowser";

    public int position;
    boolean externalBrowser;

    private OnFragmentInteractionListener mListener;

    SharedPreferences preferences;
    FragmentLinkPostBinding fragmentLinkPostBinding = null;
    NoSurfViewModel viewModel = null;

    public LinkPostFragment() {
        // Required empty public constructor
    }

    public static LinkPostFragment newInstance(int position) {
        LinkPostFragment fragment = new LinkPostFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(KEY_POSITION);
        }
        setHasOptionsMenu(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        externalBrowser = preferences.getBoolean(KEY_EXTERNAL_BROWSER, false);
        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentLinkPostBinding = FragmentLinkPostBinding.inflate(getActivity().getLayoutInflater(), container, false);
        fragmentLinkPostBinding.setViewModel(viewModel);
        fragmentLinkPostBinding.setLinkPostFragment(this);
        fragmentLinkPostBinding.setLifecycleOwner(this); //comments Transformation in ViewModel won't be called without this!

        final TextView[] comments = new TextView[3];
        final TextView[] commentsDetails = new TextView[3];
        final View[] dividers = new View[2];

        comments[0] = fragmentLinkPostBinding.linkPostFragmentFirstComment;
        comments[1] = fragmentLinkPostBinding.linkPostFragmentSecondComment;
        comments[2] = fragmentLinkPostBinding.linkPostFragmentThirdComment;

        comments[0].setMovementMethod(LinkMovementMethod.getInstance());
        comments[1].setMovementMethod(LinkMovementMethod.getInstance());
        comments[2].setMovementMethod(LinkMovementMethod.getInstance());

        commentsDetails[0] = fragmentLinkPostBinding.linkPostFragmentFirstCommentDetails;
        commentsDetails[1] = fragmentLinkPostBinding.linkPostFragmentSecondCommentDetails;
        commentsDetails[2] = fragmentLinkPostBinding.linkPostFragmentThirdCommentDetails;

        dividers[0] = fragmentLinkPostBinding.linkPostFragmentDividerUnderFirstComment;
        dividers[1] = fragmentLinkPostBinding.linkPostFragmentDividerUnderSecondComment;

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

                    fragmentLinkPostBinding.linkPostFragmentCommentProgressBar.setVisibility(View.GONE);
                }
            }
        });

        return fragmentLinkPostBinding.getRoot();
    }

    //TODO: move this to MainActivity
    public void launchWebView() {
        if (mListener != null) {
            mListener.launchWebView(viewModel.getAllPostsLiveDataViewState().getValue().postData.get(position).url, null);
        }
    }

    //TODO: move this to MainActivity
    public void launchExternalBrowser() {
        if (mListener != null) {
            //mListener.launchWebView(url, null);
            //TODO: pull this out into separate method
            Uri uri = Uri.parse(viewModel.getAllPostsLiveDataViewState().getValue().postData.get(position).url);
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