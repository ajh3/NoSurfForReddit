package com.aaronhalbert.nosurfforreddit.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.db.ReadPostId;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.List;

public class SubscribedPostsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, PostsAdapter.LoadListOfReadPostIds {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private List<ReadPostId> mReadPostIds;

    private RecyclerView rv;

    private int lastClickedRow;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Listing latestListing;

    NoSurfViewModel viewModel;

    public SubscribedPostsFragment() {
        // Required empty public constructor
    }

    public static SubscribedPostsFragment newInstance(String param1, String param2) {
        SubscribedPostsFragment fragment = new SubscribedPostsFragment();
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

        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);

        //TODO: Pull this out into a separate subscribe() method like in ChronoActivity3, and move the observer registration to onCreate, which is the recommended place for it

        viewModel.getHomePostsLiveData().observe(this, new Observer<Listing>() {
            @Override
            public void onChanged(@Nullable Listing listing) {
                latestListing = listing;
                PostsAdapter postsAdapter = (PostsAdapter) rv.getAdapter();

                postsAdapter.setCurrentListing(latestListing);
                postsAdapter.notifyDataSetChanged();

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });

        viewModel.getReadPostIdLiveData().observe(this, new Observer<List<ReadPostId>>() {
            @Override
            public void onChanged(@Nullable List<ReadPostId> readPostIds) {
                PostsAdapter postsAdapter = (PostsAdapter) rv.getAdapter();
                mReadPostIds = readPostIds;
                postsAdapter.notifyItemChanged(lastClickedRow);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_subscribed_posts, container, false);

        PostsAdapter postsAdapter = new PostsAdapter(getActivity(), (PostsAdapter.RecyclerViewOnClickCallback) getActivity(), this);

        rv = v.findViewById(R.id.home_posts_fragment_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        rv.setAdapter(postsAdapter);
        postsAdapter.setCurrentListing(latestListing);
        rv.setHasFixedSize(true);


        swipeRefreshLayout = v.findViewById(R.id.home_posts_fragment_swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        return v;
    }

    @Override
    public void onRefresh() {
        viewModel.requestHomeSubredditsListing();

    }

    public String[] getReadPostIds() {
        int size = mReadPostIds.size();
        String[] readPostIds = new String[size];

        for (int i = 0; i < size; i++) {
            readPostIds[i] = mReadPostIds.get(i).getReadPostId();
        }

        return readPostIds;
    }

    public void setLastClickedRow(int lastClickedRow) {
        this.lastClickedRow = lastClickedRow;
    }
}
