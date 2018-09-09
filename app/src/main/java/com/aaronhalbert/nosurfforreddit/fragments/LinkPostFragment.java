package com.aaronhalbert.nosurfforreddit.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.GlideApp;
import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import org.w3c.dom.Text;

import java.util.List;

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

    private String title;
    private String imageUrl;
    private String url;
    private String gifUrl;
    private String id;

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


    public static LinkPostFragment newInstance(String title, String imageUrl, String url, String gifUrl, String id) {
        LinkPostFragment fragment = new LinkPostFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_IMAGE_URL, imageUrl);
        args.putString(KEY_URL, url);
        args.putString(KEY_GIF_URL, gifUrl);
        args.putString(KEY_ID, id);
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

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);

        View v = inflater.inflate(R.layout.fragment_link_post, container, false);

        ImageView iv = v.findViewById(R.id.link_post_fragment_image);
        TextView t = v.findViewById(R.id.link_post_fragment_title);

        final TextView[] comments = new TextView[3];

        comments[0] = v.findViewById(R.id.link_post_fragment_first_comment);
        comments[1] = v.findViewById(R.id.link_post_fragment_second_comment);
        comments[2] = v.findViewById(R.id.link_post_fragment_third_comment);

        final View[] dividers = new View[2];

        dividers[0] = v.findViewById(R.id.link_post_fragment_divider_under_first_comment);
        dividers[1] = v.findViewById(R.id.link_post_fragment_divider_under_second_comment);

        GlideApp.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(iv);

        t.setText(title);

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
                        comments[i].setText(commentListing.get(1).getData().getChildren().get(i).getData().getBody());
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

    // TODO: Rename method, update argument and hook method into UI event
    public void launchWebView() {
        if (mListener != null) {
            mListener.launchWebView(url);
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
        void launchWebView(String url);
    }
}
