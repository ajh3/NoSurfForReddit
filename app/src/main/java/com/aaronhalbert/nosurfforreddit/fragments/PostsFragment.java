package com.aaronhalbert.nosurfforreddit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.PostsFragmentViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/* base fragment containing the master view of posts, in a RecyclerView
 *
 * when a row (post) is clicked, the associated PostsAdapter kicks off a PostFragment detail view */

abstract public class PostsFragment extends BaseFragment {
    @SuppressWarnings("WeakerAccess") @Inject ViewModelFactory viewModelFactory;
    PostsFragmentViewModel viewModel;
    MainActivityViewModel mainActivityViewModel;
    LiveData<PostsViewState> postsViewStateLiveData;

    private RecyclerView rv;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(PostsFragmentViewModel.class);
        mainActivityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        selectPostsViewStateLiveData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_posts, container, false);
        setupRecyclerView(v);

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
        rv.setAdapter(null);
        rv = null;
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void setupRecyclerView(View v) {
        Context context = requireContext();

        rv = v.findViewById(R.id.posts_fragment_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        rv.setAdapter(createPostsAdapter());
        rv.setHasFixedSize(true);
    }

    // endregion helper methods --------------------------------------------------------------------

    // region abstract methods ---------------------------------------------------------------------

    abstract PostsAdapter createPostsAdapter();
    abstract void selectPostsViewStateLiveData();

    // endregion abstract methods ------------------------------------------------------------------
}
