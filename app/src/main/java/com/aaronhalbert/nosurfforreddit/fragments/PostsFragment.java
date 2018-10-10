package com.aaronhalbert.nosurfforreddit.fragments;

import android.arch.lifecycle.LiveData;
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

public class PostsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, PostsAdapter.LoadListOfReadPostIds {
    private static final String KEY_IS_SUBSCRIBED_POSTS = "isSubscribedPosts";

    private boolean isSubscribedPosts;

    private List<ReadPostId> mReadPostIds;

    private RecyclerView rv;

    private int lastClickedRow;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Listing latestListing;

    NoSurfViewModel viewModel;

    public PostsFragment() {
        // Required empty public constructor
    }

    public static PostsFragment newInstance(boolean isSubscribedPosts) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_IS_SUBSCRIBED_POSTS, isSubscribedPosts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isSubscribedPosts = getArguments().getBoolean(KEY_IS_SUBSCRIBED_POSTS);
        }

        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);

        //TODO: Pull this out into a separate subscribe() method like in ChronoActivity3, and move the observer registration to onCreate, which is the recommended place for it
        //TODO: should be able to get rid of the notify call (?)

        LiveData<Listing> postsLiveData;

        if (isSubscribedPosts) {
            postsLiveData = viewModel.getAllPostsLiveData();
        } else {
            postsLiveData = viewModel.getHomePostsLiveData();
        }

        postsLiveData.observe(this, new Observer<Listing>() {
            @Override
            public void onChanged(@Nullable Listing listing) {
                Log.e(getClass().toString(), "onChanged in PostsFragment");
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
                Log.e(getClass().toString(), "Database onChanged in PostsFragment");
                PostsAdapter postsAdapter = (PostsAdapter) rv.getAdapter();
                mReadPostIds = readPostIds;
                postsAdapter.notifyItemChanged(lastClickedRow);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_posts, container, false);

        PostsAdapter postsAdapter = new PostsAdapter(getActivity(), (PostsAdapter.RecyclerViewOnClickCallback) getActivity(), this, viewModel, this);

        rv = v.findViewById(R.id.posts_fragment_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        rv.setAdapter(postsAdapter);
        postsAdapter.setCurrentListing(latestListing);
        rv.setHasFixedSize(true);


        swipeRefreshLayout = v.findViewById(R.id.posts_fragment_swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        return v;
    }


    @Override
    public void onRefresh() {
        viewModel.requestAllSubredditsListing();

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
