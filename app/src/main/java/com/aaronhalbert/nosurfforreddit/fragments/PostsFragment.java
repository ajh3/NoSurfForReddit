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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

public class PostsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String KEY_IS_SUBSCRIBED_POSTS_FRAGMENT = "isSubscribedPosts";
    private boolean isSubscribedPostsFragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostsAdapter postsAdapter;
    private NoSurfViewModel viewModel;

    public PostsFragment() { }

    public static PostsFragment newInstance(boolean isSubscribedPostsFragment) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_IS_SUBSCRIBED_POSTS_FRAGMENT, isSubscribedPostsFragment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            isSubscribedPostsFragment = getArguments().getBoolean(KEY_IS_SUBSCRIBED_POSTS_FRAGMENT);
        }

        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);
        postsAdapter = new PostsAdapter((PostsAdapter.launchPostCallback) getActivity(),
                viewModel,
                this,
                isSubscribedPostsFragment);

        LiveData<Listing> postsLiveData;

        if (isSubscribedPostsFragment) {
            postsLiveData = viewModel.getHomePostsLiveData();
        } else {
            postsLiveData = viewModel.getAllPostsLiveData();
        }

        postsLiveData.observe(this, new Observer<Listing>() {
            @Override
            public void onChanged(@Nullable Listing listing) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                //TODO: what is a more efficient way to do this?
                //ensure that strikethroughs are performed when postsLiveData is loaded w/ data
                postsAdapter.notifyDataSetChanged();
            }
        });

        viewModel.getReadPostIdsLiveData().observe(this, new Observer<String[]>() {
            @Override
            public void onChanged(@Nullable String[] readPostIds) {
                postsAdapter.setReadPostIds(readPostIds);
                //ensure last clicked post is struck through when going BACK to PostsFragment
                postsAdapter.notifyItemChanged(postsAdapter.getLastClickedRow());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_posts, container, false);

        RecyclerView rv = v.findViewById(R.id.posts_fragment_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        rv.setAdapter(postsAdapter);
        rv.setHasFixedSize(true);

        swipeRefreshLayout = v.findViewById(R.id.posts_fragment_swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        return v;
    }

    @Override
    public void onRefresh() {
        if (isSubscribedPostsFragment) {
            viewModel.requestHomeSubredditsListing();
        } else {
            viewModel.requestAllSubredditsListing();
        }
    }
}
