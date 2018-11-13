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

/* base fragment containing the master view of posts, in a RecyclerView
 *
 * when a row (post) is clicked, the associated PostsAdapter kicks off a PostFragment detail view */

abstract class PostsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    NoSurfViewModel viewModel;
    LiveData<PostsViewState> postsViewStateLiveData;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(requireActivity()).get(NoSurfViewModel.class);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        /* if we don't remove the adapter in onDestroyView(), we end up leaking a view to the
         * data binding class. This happens because the binding class references
         * PostsAdapter.RowHolder as its controller, and the adapter references this fragment
         * to provide the data binding class access to a LifecycleOwner. Thus, when PostsFragment
         * is replace()'d (detached but not destroyed), the adapter stays alive on the backstack
         * due to the data binding class's reference to it, which in turn keeps a reference back to
         * the fragment's view hierarchy, having prevented it from being properly destroyed in
         * onDestroyView(). */
        RecyclerView rv = getView().findViewById(R.id.posts_fragment_recyclerview);
        rv.setAdapter(null);
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
        rv.setAdapter(createPostsAdapter());
        rv.setHasFixedSize(true);
    }

    private void setupSwipeRefreshLayout(View v) {
        SwipeRefreshLayout s = v.findViewById(R.id.posts_fragment_swipe_refresh_layout);
        s.setOnRefreshListener(this);
    }

    // endregion helper methods --------------------------------------------------------------------

    // region abstract methods ---------------------------------------------------------------------

    abstract PostsAdapter createPostsAdapter();
    abstract void selectPostsViewStateLiveData();

    // endregion abstract methods ------------------------------------------------------------------
}
