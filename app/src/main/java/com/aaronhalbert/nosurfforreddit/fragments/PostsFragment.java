package com.aaronhalbert.nosurfforreddit.fragments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

abstract public class PostsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    SwipeRefreshLayout swipeRefreshLayout;
    PostsAdapter postsAdapter;
    NoSurfViewModel viewModel;
    LiveData<Listing> postsLiveData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);
        postsAdapter = newPostsAdapter();
        setPostsLiveData();

        postsLiveData.observe(this, listing -> {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            //TODO: what is a more efficient way to do this?
            //ensure that strikethroughs are performed when postsLiveData is loaded w/ data
            postsAdapter.notifyDataSetChanged();
        });

        viewModel.getReadPostIdsLiveData().observe(this, readPostIds -> {
            postsAdapter.setReadPostIds(readPostIds);
            //ensure last clicked post is struck through when going BACK to PostsFragment
            postsAdapter.notifyItemChanged(postsAdapter.getLastClickedRow());
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

    abstract PostsAdapter newPostsAdapter();
    abstract void setPostsLiveData();
}
