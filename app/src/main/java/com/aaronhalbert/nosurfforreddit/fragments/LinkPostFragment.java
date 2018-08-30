package com.aaronhalbert.nosurfforreddit.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.GlideApp;
import com.aaronhalbert.nosurfforreddit.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LinkPostFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LinkPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LinkPostFragment extends Fragment {
    private static final String KEY_IMAGE_URL = "imageUrl";
    private static final String KEY_URL = "url";
    private static final String KEY_TITLE = "title";

    private String imageUrl;
    private String url;
    private String title;

    private OnFragmentInteractionListener mListener;

    public LinkPostFragment() {
        // Required empty public constructor
    }


    public static LinkPostFragment newInstance(String imageUrl, String url, String title) {
        LinkPostFragment fragment = new LinkPostFragment();
        Bundle args = new Bundle();
        args.putString(KEY_IMAGE_URL, imageUrl);
        args.putString(KEY_URL, url);
        args.putString(KEY_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(KEY_IMAGE_URL);
            url = getArguments().getString(KEY_URL);
            title = getArguments().getString(KEY_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_link_post, container, false);

        ImageView iv = v.findViewById(R.id.link_post_fragment_image);

        iv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e(getClass().toString(), url);

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });

        GlideApp.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(iv);

        TextView t = v.findViewById(R.id.link_post_fragment_title);

        t.setText(title);

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
/*
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
*/
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
        void onFragmentInteraction(Uri uri);
    }
}
