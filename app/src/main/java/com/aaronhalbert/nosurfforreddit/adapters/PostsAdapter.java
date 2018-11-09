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
    private static final int ITEM_COUNT = 25;

    private final NoSurfViewModel viewModel;
    private final Fragment hostFragment;
    private final boolean isSubscribedPostsAdapter;
    private final LiveData<PostsViewState> postsLiveDataViewState;

    public PostsAdapter(NoSurfViewModel viewModel,
                        Fragment hostFragment,
                        boolean isSubscribedPostsAdapter) {

        this.viewModel = viewModel;
        this.hostFragment = hostFragment;
        this.isSubscribedPostsAdapter = isSubscribedPostsAdapter;

        if (isSubscribedPostsAdapter) {
            postsLiveDataViewState = viewModel.getSubscribedPostsViewStateLiveData();
        } else {
            postsLiveDataViewState = viewModel.getAllPostsViewStateLiveData();
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
        public LiveData<PostsViewState> getPostsLiveDataViewState() {
            return postsLiveDataViewState;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            viewModel.setLastClickedPostMetadata(new LastClickedPostMetadata(
                    position,
                    postsLiveDataViewState.getValue().postData.get(position).id,
                    postsLiveDataViewState.getValue().postData.get(position).isSelf,
                    isSubscribedPostsAdapter));

            viewModel.setPostsFragmentClickEventsLiveData(true);
        }
    }

    // endregion helper classes---------------------------------------------------------------------
}
