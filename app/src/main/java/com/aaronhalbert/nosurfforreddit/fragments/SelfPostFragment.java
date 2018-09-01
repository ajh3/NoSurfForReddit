package com.aaronhalbert.nosurfforreddit.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.R;

import static android.view.View.GONE;

public class SelfPostFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String KEY_TITLE = "title";
    private static final String KEY_SELFTEXT = "selfText";

    // TODO: Rename and change types of parameters
    private String title;
    private String selfText;

    private OnFragmentInteractionListener mListener;

    public SelfPostFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SelfPostFragment newInstance(String title, String selfText) {
        SelfPostFragment fragment = new SelfPostFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_SELFTEXT, selfText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(KEY_TITLE);
            selfText = getArguments().getString(KEY_SELFTEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_self_post, container, false);

        TextView tv = v.findViewById(R.id.self_post_fragment_title);

        tv.setText(title);

        TextView st = v.findViewById(R.id.self_post_fragment_selftext);

        Log.e(getClass().toString(), "selftext is: " + selfText);

        if (selfText == null) {
            st.setVisibility(GONE);
        } else {
            st.setText(selfText);
        }



        // Inflate the layout for this fragment
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
