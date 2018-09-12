package com.aaronhalbert.nosurfforreddit.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.List;

import static android.text.Html.FROM_HTML_MODE_LEGACY;
import static android.text.Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE;
import static android.text.Html.toHtml;
import static android.view.View.GONE;

public class SelfPostFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String KEY_TITLE = "title";
    private static final String KEY_SELFTEXT = "selfText";
    private static final String KEY_ID = "id";
    private static final String KEY_SUBREDDIT = "subreddit";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_SCORE = "score";

    // TODO: Rename and change types of parameters
    private String title;
    private String selfText;
    private String id;
    private String subreddit;
    private String author;
    private int score;

    NoSurfViewModel viewModel = null;

    TextView firstComment = null;
    TextView secondComment = null;
    TextView thirdComment = null;

    View secondDivider = null;
    View thirdDivider = null;

    public SelfPostFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SelfPostFragment newInstance(String title, String selfText, String id, String subreddit, String author, int score) {
        SelfPostFragment fragment = new SelfPostFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_SELFTEXT, selfText);
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
            selfText = getArguments().getString(KEY_SELFTEXT);
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

        View v = inflater.inflate(R.layout.fragment_self_post, container, false);

        TextView t = v.findViewById(R.id.self_post_fragment_title);
        TextView st = v.findViewById(R.id.self_post_fragment_selftext);
        TextView details = v.findViewById(R.id.self_post_fragment_details);

        final View dividerUnderSelftext = v.findViewById(R.id.self_post_fragment_divider_under_selftext);

        final TextView[] comments = new TextView[3];

        comments[0] = v.findViewById(R.id.self_post_fragment_first_comment);
        comments[1] = v.findViewById(R.id.self_post_fragment_second_comment);
        comments[2] = v.findViewById(R.id.self_post_fragment_third_comment);

        final TextView[] commentsDetails = new TextView[3];

        commentsDetails[0] = v.findViewById(R.id.self_post_fragment_first_comment_details);
        commentsDetails[1] = v.findViewById(R.id.self_post_fragment_second_comment_details);
        commentsDetails[2] = v.findViewById(R.id.self_post_fragment_third_comment_details);

        final View[] dividers = new View[2];

        dividers[0] = v.findViewById(R.id.self_post_fragment_divider_under_first_comment);
        dividers[1] = v.findViewById(R.id.self_post_fragment_divider_under_second_comment);

        t.setText(title);

        String postDetails = "in r/" + subreddit + " by u/" + author + "\u0020\u2022\u0020" + score;
        details.setText(postDetails);

        if (!(selfText == null) && !(selfText.isEmpty())) {
            if (Build.VERSION.SDK_INT >= 24) {

                String unescaped = Html.fromHtml(selfText, FROM_HTML_MODE_LEGACY).toString();
                Spanned formatted = Html.fromHtml(unescaped, FROM_HTML_MODE_LEGACY);
                Spanned trailingNewLinesStripped = (Spanned) trimTrailingWhitespace(formatted);
                st.setText(trailingNewLinesStripped);
                st.setVisibility(View.VISIBLE);

            } else {

                String unescaped = Html.fromHtml(selfText).toString();
                Spanned formatted = Html.fromHtml(unescaped);
                Spanned trailingNewLinesStripped = (Spanned) trimTrailingWhitespace(formatted);
                st.setText(trailingNewLinesStripped);
                st.setVisibility(View.VISIBLE);
            }
        }

        //TODO: Pull this out into a separate subscribe() method like in ChronoActivity3, and move the observer registration to onCreate, which is the recommended place for it
        viewModel.getCommentsLiveData().observe(this, new Observer<List<Listing>>() {
            @Override
            public void onChanged(@Nullable List<Listing> commentListing) {
                if (id.equals(commentListing.get(0).getData().getChildren().get(0).getData().getId())) {
                    int numComments = commentListing.get(1).getData().getChildren().size();
                    if (numComments > 3) numComments = 3;

                    if ((numComments > 0) && !(selfText == null) && !(selfText.isEmpty())) dividerUnderSelftext.setVisibility(View.VISIBLE);

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_link_and_self_post_fragments, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                return true;
            case R.id.about:
                return true;
        }
        return (super.onOptionsItemSelected(item)); //what does this do?
    }
}
