package com.aaronhalbert.nosurfforreddit.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.List;

import static android.view.View.GONE;

public class SelfPostFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String KEY_TITLE = "title";
    private static final String KEY_SELFTEXT = "selfText";
    private static final String KEY_ID = "id";

    // TODO: Rename and change types of parameters
    private String title;
    private String selfText;
    private String id;

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
    public static SelfPostFragment newInstance(String title, String selfText, String id) {
        SelfPostFragment fragment = new SelfPostFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_SELFTEXT, selfText);
        args.putString(KEY_ID, id);
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
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);

        View v = inflater.inflate(R.layout.fragment_self_post, container, false);

        TextView t = v.findViewById(R.id.self_post_fragment_title);
        TextView st = v.findViewById(R.id.self_post_fragment_selftext);
        firstComment = v.findViewById(R.id.self_post_fragment_first_comment);
        secondComment = v.findViewById(R.id.self_post_fragment_second_comment);
        thirdComment = v.findViewById(R.id.self_post_fragment_third_comment);

        secondDivider = v.findViewById(R.id.self_post_fragment_second_divider);
        thirdDivider = v.findViewById(R.id.self_post_fragment_third_divider);

        t.setText(title);

        if (selfText.isEmpty()) {
            st.setVisibility(GONE);
        } else {
            st.setText(selfText);
        }

        //TODO: Pull this out into a separate subscribe() method like in ChronoActivity3, and move the observer registration to onCreate, which is the recommended place for it
        viewModel.getCommentsLiveData().observe(this, new Observer<List<Listing>>() {
            @Override
            public void onChanged(@Nullable List<Listing> commentListing) {
                if (id.equals(commentListing.get(0).getData().getChildren().get(0).getData().getId())) {
                    firstComment.setText(commentListing.get(1).getData().getChildren().get(0).getData().getBody());
                    secondComment.setText(commentListing.get(1).getData().getChildren().get(1).getData().getBody());
                    thirdComment.setText(commentListing.get(1).getData().getChildren().get(2).getData().getBody());

                    secondDivider.setVisibility(View.VISIBLE);
                    thirdDivider.setVisibility(View.VISIBLE);
                }
                //TODO: And shouldn't this observer go out of scope and stop working after onViewCreated finishes?
            }
        });
        return v;
    }
}
