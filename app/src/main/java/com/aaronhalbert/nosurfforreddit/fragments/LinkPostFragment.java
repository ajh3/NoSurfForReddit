package com.aaronhalbert.nosurfforreddit.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.GlideApp;
import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.databinding.FragmentLinkPostBinding;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.List;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

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

        //TODO: bind
        GlideApp.with(this)
                .load(viewModel.getAllPostsLiveData().getValue().getData().getChildren().get(position).getData().getPreview().getImages().get(0).getSource().getUrl())
                .centerCrop()
                .into(fragmentLinkPostBinding.linkPostFragmentImage);





        //TODO: perform the data manipulation in the ViewModel and bind the results, this should only contain UI logic
        viewModel.getCommentsSingleLiveEvent().observe(this, new Observer<List<Listing>>() {
            @Override
            public void onChanged(@Nullable List<Listing> commentListing) {
                fragmentLinkPostBinding.linkPostFragmentCommentProgressBar.setVisibility(View.VISIBLE);

                if ((viewModel.getAllPostsLiveData().getValue().getData().getChildren().get(position).getData().getId().equals(commentListing.get(0).getData().getChildren().get(0).getData().getId()))
                        && (commentListing.get(0).getData().getChildren().get(0).getData().getNumComments() > 0)) {
                    int autoModOffset;

                    //skip first comment if it's by AutoMod
                    if ((commentListing.get(1).getData().getChildren().get(0).getData().getAuthor()).equals("AutoModerator")) {
                        autoModOffset = 1;
                    } else {
                        autoModOffset = 0;
                    }

                    int numTopLevelComments = commentListing.get(1).getData().getChildren().size();
                    numTopLevelComments = numTopLevelComments - autoModOffset; // avoid running past array in cases where numTopLevelComments < 4 and one of them is an AutoMod post
                    if (numTopLevelComments > 3) numTopLevelComments = 3;

                    for (int i = 0; i < numTopLevelComments; i++) {
                        if (Build.VERSION.SDK_INT >= 24) {
                            String unescaped = Html.fromHtml(commentListing.get(1)
                                    .getData()
                                    .getChildren()
                                    .get(autoModOffset + i)
                                    .getData()
                                    .getBodyHtml(), FROM_HTML_MODE_LEGACY).toString();
                            Spanned formatted = Html.fromHtml(unescaped, FROM_HTML_MODE_LEGACY);
                            Spanned trailingNewLinesStripped = (Spanned) trimTrailingWhitespace(formatted);
                            comments[i].setText(trailingNewLinesStripped);
                            comments[i].setVisibility(View.VISIBLE);

                            String commentAuthor = commentListing.get(1).getData().getChildren().get(autoModOffset + i).getData().getAuthor();
                            int commentScore = commentListing.get(1).getData().getChildren().get(autoModOffset + i).getData().getScore();

                            String commentDetails = "u/" + commentAuthor + " \u2022 " + Integer.toString(commentScore);

                            commentsDetails[i].setText(commentDetails);
                            commentsDetails[i].setVisibility(View.VISIBLE);
                        } else {
                            String unescaped = Html.fromHtml(commentListing.get(1)
                                    .getData()
                                    .getChildren()
                                    .get(autoModOffset + i)
                                    .getData()
                                    .getBodyHtml()).toString();
                            Spanned formatted = Html.fromHtml(unescaped);
                            Spanned trailingNewLinesStripped = (Spanned) trimTrailingWhitespace(formatted);
                            comments[i].setText(trailingNewLinesStripped);
                            comments[i].setVisibility(View.VISIBLE);

                            String commentAuthor = commentListing.get(1).getData().getChildren().get(autoModOffset + i).getData().getAuthor();
                            int commentScore = commentListing.get(1).getData().getChildren().get(autoModOffset + i).getData().getScore();

                            String commentDetails = "u/" + commentAuthor + " \u2022 " + Integer.toString(commentScore);

                            commentsDetails[i].setText(commentDetails);
                            commentsDetails[i].setVisibility(View.VISIBLE);
                        }
                    }

                    for (int i = 0; i < (numTopLevelComments - 1); i++) {
                        dividers[i].setVisibility(View.VISIBLE);
                    }
                }
                fragmentLinkPostBinding.linkPostFragmentCommentProgressBar.setVisibility(View.GONE);
            }
        });
        return fragmentLinkPostBinding.getRoot();
    }

    CharSequence trimTrailingWhitespace(CharSequence source) {
        if (source == null) return "";

        int i = source.length();

        //decrement i and check if that character is whitespace
        do { --i; } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        //tick i up by 1 to return the full non-whitespace sequence
        return source.subSequence(0, i+1);
    }

    //TODO: move this to MainActivity
    public void launchWebView() {
        if (mListener != null) {
            mListener.launchWebView(viewModel.getAllPostsLiveData().getValue().getData().getChildren().get(position).getData().getUrl(), null);
        }
    }

    //TODO: move this to MainActivity
    public void launchExternalBrowser() {
        if (mListener != null) {
            //mListener.launchWebView(url, null);
            //TODO: pull this out into separate method
            Uri uri = Uri.parse(viewModel.getAllPostsLiveData().getValue().getData().getChildren().get(position).getData().getUrl());
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
