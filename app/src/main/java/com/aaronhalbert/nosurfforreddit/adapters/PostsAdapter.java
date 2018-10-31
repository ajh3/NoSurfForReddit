package com.aaronhalbert.nosurfforreddit.adapters;

import androidx.lifecycle.LiveData;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.viewstate.LastClickedPostMetadata;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;
import com.aaronhalbert.nosurfforreddit.databinding.RowSinglePostBinding;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.RowHolder> {
    private static final int ITEM_COUNT = 25;

    private launchPostCallback launchPostCallback;
    private NoSurfViewModel viewModel;
    private Fragment hostFragment;
    private boolean isSubscribedPostsAdapter;
    private LiveData<PostsViewState> postsLiveDataViewState;

    public PostsAdapter(launchPostCallback launchPostCallback,
                        NoSurfViewModel viewModel,
                        Fragment hostFragment,
                        boolean isSubscribedPostsAdapter) {

        this.launchPostCallback = launchPostCallback;
        this.viewModel = viewModel;
        this.hostFragment = hostFragment;
        this.isSubscribedPostsAdapter = isSubscribedPostsAdapter;

        if (isSubscribedPostsAdapter) {
            postsLiveDataViewState = viewModel.getStitchedSubscribedPostsLiveDataViewState();
        } else {
            postsLiveDataViewState = viewModel.getStitchedAllPostsLiveDataViewState();
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

            launchPostCallback.launchPost();
        }
    }

    // endregion helper classes---------------------------------------------------------------------

    // region interfaces ---------------------------------------------------------------------------

    public interface launchPostCallback {
        void launchPost();
    }

    // endregion interfaces ------------------------------------------------------------------------
}
