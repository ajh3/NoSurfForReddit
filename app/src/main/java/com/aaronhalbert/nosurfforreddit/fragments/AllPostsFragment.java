package com.aaronhalbert.nosurfforreddit.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.Objects;

public class AllPostsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView rv = null;

    private SwipeRefreshLayout swipeRefreshLayout = null;

    NoSurfViewModel viewModel = null;

    public AllPostsFragment() {
        // Required empty public constructor
    }

    public static AllPostsFragment newInstance(String param1, String param2) {
        AllPostsFragment fragment = new AllPostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);

        //TODO: Pull this out into a separate subscribe() method like in ChronoActivity3, and move the observer registration to onCreate, which is the recommended place for it
        viewModel.getAllPostsLiveData().observe(this, new Observer<Listing>() {
            @Override
            public void onChanged(@Nullable Listing listing) {
                Log.e(getClass().toString(), "onChanged");
                //TODO: why is onChanged called twice on config change?
                //TODO: And shouldn't this observer go out of scope and stop working after onViewCreated finishes?

                ((PostsAdapter) rv.getAdapter()).setCurrentListing(listing);
                ((PostsAdapter) rv.getAdapter()).notifyDataSetChanged();

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });

        rv = Objects.requireNonNull(getView()).findViewById(R.id.all_posts_fragment_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new PostsAdapter(getActivity(), (PostsAdapter.RecyclerViewOnClickCallback) getActivity()));
        rv.setHasFixedSize(true);

        swipeRefreshLayout = getView().findViewById(R.id.all_posts_fragment_swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

    }


    @Override
    public void onRefresh() {
        viewModel.requestAllSubredditsListing();

    }

}
