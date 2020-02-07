/*
 * Copyright (c) 2018-present, Aaron J. Halbert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.aaronhalbert.nosurfforreddit.ui.master;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aaronhalbert.nosurfforreddit.BaseFragment;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.data.local.settings.PreferenceSettingsStore;
import com.aaronhalbert.nosurfforreddit.ui.main.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.ui.viewstate.PostsViewState;
import com.aaronhalbert.nosurfforreddit.utils.ViewModelFactory;

import java.util.ArrayList;

import javax.inject.Inject;

import static com.aaronhalbert.nosurfforreddit.ui.viewstate.PostsViewState.Post;

/* base fragment containing the master view of posts, in a RecyclerView
 *
 * when a row (post) is clicked, the associated PostsAdapter kicks off a PostFragment detail view */

abstract public class PostsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final int ITEM_COUNT = 25;

    @SuppressWarnings("WeakerAccess") @Inject public PreferenceSettingsStore preferenceSettingsStore;
    @SuppressWarnings("WeakerAccess") @Inject ViewModelFactory viewModelFactory;
    MainActivityViewModel viewModel;
    LiveData<PostsViewState> postsViewStateLiveData;

    private RecyclerView rv;
    private SwipeRefreshLayout swipeRefreshLayout;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
                .get(MainActivityViewModel.class);
        selectPostsViewStateLiveData();
        setupRefreshAnimationCanceler();
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

        /* if we don't null out the adapter in onDestroyView(), we end up leaking a view to the
         * data binding class when this fragment goes onto the backstack. This happens because the
         * binding class references PostsAdapter.RowHolder as its controller, and the adapter
         * references this fragment to provide the data binding class access to a LifecycleOwner.
         * Thus, when PostsFragment is replace()'d (detached but not destroyed), the adapter stays
         * alive on the backstack due to the data binding class's reference to it, which in turn
         * keeps a reference back to the fragment and its view hierarchy, preventing it from being
         * properly torn down in onDestroyView().
         *
         * Similar thing with the two instance variables that reference Views - if not nulled here,
         * these will prevent the Views from being torn down when the fragment goes on the
         * backstack. */
        rv.setAdapter(null);
        rv = null;
        swipeRefreshLayout = null;
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region menu ---------------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.refresh:
                refreshWithAnimation();
                return true;
            case R.id.mark_all_as_read:
                ArrayList<Post> postData = postsViewStateLiveData.getValue().postData;

                for (int i = 0; i < ITEM_COUNT; i++) {
                    viewModel.insertClickedPostId(postData.get(i).id);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // endregion menu ------------------------------------------------------------------------------

    // region observers ----------------------------------------------------------------------------

    private void setupRefreshAnimationCanceler() {
        postsViewStateLiveData.observe(this, listing -> cancelRefreshAnimation());
    }

    // endregion observers -------------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void setupRecyclerView(View v) {
        Context context = requireContext();

        rv = v.findViewById(R.id.posts_fragment_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        rv.setAdapter(createPostsAdapter());
        rv.setHasFixedSize(true);
    }

    private void setupSwipeRefreshLayout(View v) {
        swipeRefreshLayout = v.findViewById(R.id.posts_fragment_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setEnabled(!preferenceSettingsStore.isSwipeToRefreshDisabled());
    }

    private void refreshWithAnimation() {
        swipeRefreshLayout.post(() -> {
            setSwipeRefreshLayoutRefreshStatus(true);
            onRefresh();
        });
    }

    private void cancelRefreshAnimation() {
        if (swipeRefreshLayout.isRefreshing()) {
            setSwipeRefreshLayoutRefreshStatus(false);
        }
    }

    private void setSwipeRefreshLayoutRefreshStatus(boolean b) {
        swipeRefreshLayout.setRefreshing(b);
    }

    // endregion helper methods --------------------------------------------------------------------

    // region abstract methods ---------------------------------------------------------------------

    abstract PostsAdapter createPostsAdapter();
    abstract void selectPostsViewStateLiveData();

    // endregion abstract methods ------------------------------------------------------------------
}
