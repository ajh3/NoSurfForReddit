package com.aaronhalbert.nosurfforreddit.adapters;

import androidx.lifecycle.LiveData;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.viewmodel.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.viewstate.LastClickedPostMetadata;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;
import com.aaronhalbert.nosurfforreddit.databinding.RowSinglePostBinding;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.RowHolder> {

    // we only ever show the first page of posts, which is 25 by default
    private static final int ITEM_COUNT = 25;

    private final NoSurfViewModel viewModel;
    private final Fragment hostFragment;
    private final LiveData<PostsViewState> postsViewStateLiveData;

    /* this app has two primary screens/modes, a feed of posts from r/all (Reddit's public home
     * page, and a feed of posts from the user's subscribed subreddits (if the user is logged in).
     *
     * any field/method referring to "AllPosts" refers to the former, and any field/method
     * referring to "SubscribedPosts" refers to the latter.
     *
     * Many components, such as this adapter, are easily reused for either feed. For example,
     * all that's necessary to configure this adapter is to pass it the boolean argument
     * isSubscribedPostsAdapter in the constructor, and it sets own its data source
     * (postsViewStateLiveData) and functions accordingly. */
    private final boolean isSubscribedPostsAdapter;

    public PostsAdapter(NoSurfViewModel viewModel,
                        Fragment hostFragment,
                        boolean isSubscribedPostsAdapter) {

        this.viewModel = viewModel;
        this.hostFragment = hostFragment;
        this.isSubscribedPostsAdapter = isSubscribedPostsAdapter;

        if (isSubscribedPostsAdapter) {
            postsViewStateLiveData = viewModel.getSubscribedPostsViewStateLiveData();
        } else {
            postsViewStateLiveData = viewModel.getAllPostsViewStateLiveData();
        }
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }

    @Override
    public RowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowSinglePostBinding rowSinglePostBinding = RowSinglePostBinding
                .inflate(hostFragment.getLayoutInflater(), parent, false);
        rowSinglePostBinding.setLifecycleOwner(hostFragment);

        return new RowHolder(rowSinglePostBinding);
    }

    @Override
    public void onBindViewHolder(RowHolder rowHolder, int position) {
        rowHolder.bindModel();
    }

    // region helper classes -----------------------------------------------------------------------

    public class RowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final RowSinglePostBinding rowSinglePostBinding;

        RowHolder(RowSinglePostBinding rowSinglePostBinding) {
            super(rowSinglePostBinding.getRoot());
            this.rowSinglePostBinding = rowSinglePostBinding;
            itemView.setOnClickListener(this);
        }

        void bindModel() {
            rowSinglePostBinding.setController(this);
            rowSinglePostBinding.executePendingBindings();
        }

        //placed here so data binding class can access it
        public LiveData<PostsViewState> getPostsViewStateLiveData() {
            return postsViewStateLiveData;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            viewModel.setLastClickedPostMetadata(new LastClickedPostMetadata(
                    position,
                    postsViewStateLiveData.getValue().postData.get(position).id,
                    postsViewStateLiveData.getValue().postData.get(position).isSelf,
                    isSubscribedPostsAdapter));

            viewModel.setRecyclerViewClickEventsLiveData(true);
        }
    }

    // endregion helper classes---------------------------------------------------------------------
}
