package com.aaronhalbert.nosurfforreddit.fragments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.viewmodel.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

/* base fragment for the master view of posts in a RecyclerView
 *
 * when a row (post) is clicked, the associated PostsAdapter kicks off a PostFragment detail view */

abstract class PostsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    PostsAdapter postsAdapter;
    NoSurfViewModel viewModel;
    LiveData<PostsViewState> postsViewStateLiveData;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(requireActivity()).get(NoSurfViewModel.class);
        createPostsAdapter();
        selectPostsViewStateLiveData();
        observePostsViewStateLiveData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_posts, container, false);

        setupRecyclerView(v);
        setupSwipeRefreshLayout(v);

        return v;
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void observePostsViewStateLiveData() {
        postsViewStateLiveData.observe(this, listing -> cancelRefreshingAnimation());
    }

    private void cancelRefreshingAnimation() {
        SwipeRefreshLayout s = getView().findViewById(R.id.posts_fragment_swipe_refresh_layout);

        if (s.isRefreshing()) {
            s.setRefreshing(false);
        }
    }

    private void setupRecyclerView(View v) {
        Context context = requireContext();

        RecyclerView rv = v.findViewById(R.id.posts_fragment_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        rv.setAdapter(postsAdapter);
        rv.setHasFixedSize(true);
    }

    private void setupSwipeRefreshLayout(View v) {
        SwipeRefreshLayout s = v.findViewById(R.id.posts_fragment_swipe_refresh_layout);
        s.setOnRefreshListener(this);
    }

    // endregion helper methods --------------------------------------------------------------------

    // region abstract methods ---------------------------------------------------------------------

    abstract void createPostsAdapter();
    abstract void selectPostsViewStateLiveData();

    // endregion abstract methods ------------------------------------------------------------------
}
