package com.aaronhalbert.nosurfforreddit.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.GlideApp;
import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.R;
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
public class LinkPostFragment extends Fragment {
    private static final String KEY_TITLE = "title";
    private static final String KEY_IMAGE_URL = "imageUrl";
    private static final String KEY_URL = "url";
    private static final String KEY_GIF_URL = "gifUrl";
    private static final String KEY_ID = "id";
    private static final String KEY_SUBREDDIT = "subreddit";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_SCORE = "score";

    private String title;
    private String imageUrl;
    private String url;
    private String gifUrl;
    private String id;
    private String subreddit;
    private String author;
    private int score;

    private OnFragmentInteractionListener mListener;

    NoSurfViewModel viewModel = null;

    TextView firstComment = null;
    TextView secondComment = null;
    TextView thirdComment = null;

    View secondDivider = null;
    View thirdDivider = null;

    public LinkPostFragment() {
        // Required empty public constructor
    }


    public static LinkPostFragment newInstance(String title, String imageUrl, String url, String gifUrl, String id, String subreddit, String author, int score) {
        LinkPostFragment fragment = new LinkPostFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_IMAGE_URL, imageUrl);
        args.putString(KEY_URL, url);
        args.putString(KEY_GIF_URL, gifUrl);
        args.putString(KEY_ID, id);
        args.putString(KEY_SUBREDDIT, subreddit);
        args.putString(KEY_AUTHOR, author);
        args.putInt(KEY_SCORE, score);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(KEY_TITLE);
            imageUrl = getArguments().getString(KEY_IMAGE_URL);
            url = getArguments().getString(KEY_URL);
            gifUrl = getArguments().getString(KEY_GIF_URL);
            id = getArguments().getString(KEY_ID);
            subreddit = getArguments().getString(KEY_SUBREDDIT);
            author = getArguments().getString(KEY_AUTHOR);
            score = getArguments().getInt(KEY_SCORE);

        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);

        View v = inflater.inflate(R.layout.fragment_link_post, container, false);

        ImageView iv = v.findViewById(R.id.link_post_fragment_image);
        TextView t = v.findViewById(R.id.link_post_fragment_title);
        TextView details = v.findViewById(R.id.link_post_fragment_details);

        final TextView[] comments = new TextView[3];

        comments[0] = v.findViewById(R.id.link_post_fragment_first_comment);
        comments[1] = v.findViewById(R.id.link_post_fragment_second_comment);
        comments[2] = v.findViewById(R.id.link_post_fragment_third_comment);

        final TextView[] commentsDetails = new TextView[3];

        commentsDetails[0] = v.findViewById(R.id.link_post_fragment_first_comment_details);
        commentsDetails[1] = v.findViewById(R.id.link_post_fragment_second_comment_details);
        commentsDetails[2] = v.findViewById(R.id.link_post_fragment_third_comment_details);

        final View[] dividers = new View[2];

        dividers[0] = v.findViewById(R.id.link_post_fragment_divider_under_first_comment);
        dividers[1] = v.findViewById(R.id.link_post_fragment_divider_under_second_comment);

        GlideApp.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(iv);

        t.setText(title);

        String postDetails = "in r/" + subreddit + " by u/" + author + "\u0020\u2022\u0020" + score;
        details.setText(postDetails);

        //TODO: launch in external browser?
        iv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                launchWebView();

            }
        });

        //TODO: Pull this out into a separate subscribe() method like in ChronoActivity3, and move the observer registration to onCreate, which is the recommended place for it
        viewModel.getCommentsLiveData().observe(this, new Observer<List<Listing>>() {
            @Override
            public void onChanged(@Nullable List<Listing> commentListing) {
                if (id.equals(commentListing.get(0).getData().getChildren().get(0).getData().getId())) {
                    int numComments = commentListing.get(1).getData().getChildren().size();
                    if (numComments > 3) numComments = 3;

                    for (int i = 0; i < numComments; i++) {
                        if (Build.VERSION.SDK_INT >= 24) {
                            String unescaped = Html.fromHtml(commentListing.get(1)
                                    .getData()
                                    .getChildren()
                                    .get(i)
                                    .getData()
                                    .getBodyHtml(), FROM_HTML_MODE_LEGACY).toString();
                            Spanned formatted = Html.fromHtml(unescaped, FROM_HTML_MODE_LEGACY);
                            Spanned trailingNewLinesStripped = (Spanned) trimTrailingWhitespace(formatted);
                            comments[i].setText(trailingNewLinesStripped);
                            comments[i].setVisibility(View.VISIBLE);

                            String commentAuthor = commentListing.get(1).getData().getChildren().get(i).getData().getAuthor();
                            int commentScore = commentListing.get(1).getData().getChildren().get(i).getData().getScore();

                            String commentDetails = "u/" + commentAuthor + " \u2022 " + Integer.toString(commentScore);

                            commentsDetails[i].setText(commentDetails);
                            commentsDetails[i].setVisibility(View.VISIBLE);
                        } else {
                            String unescaped = Html.fromHtml(commentListing.get(1)
                                    .getData()
                                    .getChildren()
                                    .get(i)
                                    .getData()
                                    .getBodyHtml()).toString();
                            Spanned formatted = Html.fromHtml(unescaped);
                            Spanned trailingNewLinesStripped = (Spanned) trimTrailingWhitespace(formatted);
                            comments[i].setText(trailingNewLinesStripped);
                            comments[i].setVisibility(View.VISIBLE);

                            String commentAuthor = commentListing.get(1).getData().getChildren().get(i).getData().getAuthor();
                            int commentScore = commentListing.get(1).getData().getChildren().get(i).getData().getScore();

                            String commentDetails = "u/" + commentAuthor + " \u2022 " + Integer.toString(commentScore);

                            commentsDetails[i].setText(commentDetails);
                            commentsDetails[i].setVisibility(View.VISIBLE);
                        }
                    }

                    for (int i = 0; i < (numComments - 1); i++) {
                        dividers[i].setVisibility(View.VISIBLE);
                    }
                }
                //TODO: And shouldn't this observer go out of scope and stop working after onViewCreated finishes?
            }
        });
        return v;
    }

    CharSequence trimTrailingWhitespace(CharSequence source) {
        if (source == null) return "";

        int i = source.length();

        //decrement i and check if that character is whitespace
        do { --i; } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        //tick i up by 1 to return the full non-whitespace sequence
        return source.subSequence(0, i+1);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void launchWebView() {
        if (mListener != null) {
            //mListener.launchWebView(url, null);
            //TODO: pull this out into separate method, no longer need listener?
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
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
        // TODO: Update argument type and name
        void launchWebView(String url, String tag);
    }
}
