package com.aaronhalbert.nosurfforreddit.adapters;

import androidx.lifecycle.LiveData;
import android.graphics.Paint;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;
import com.aaronhalbert.nosurfforreddit.databinding.RowSinglePostBinding;

import java.util.Arrays;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.RowHolder> {
    private static final int ITEM_COUNT = 25;

    private launchPostCallback launchPostCallback;
    private NoSurfViewModel viewModel;
    private Fragment hostFragment;
    private boolean isSubscribedPostsAdapter;
    private LiveData<PostsViewState> postsLiveDataViewState;
    private int lastClickedRow;
    //initialize to empty array in case it is read from before written to by Observer
    private String[] clickedPostIds = new String[0];

    public PostsAdapter(launchPostCallback launchPostCallback,
                        NoSurfViewModel viewModel,
                        Fragment hostFragment,
                        boolean isSubscribedPostsAdapter) {

        this.launchPostCallback = launchPostCallback;
        this.viewModel = viewModel;
        this.hostFragment = hostFragment;
        this.isSubscribedPostsAdapter = isSubscribedPostsAdapter;

        if (isSubscribedPostsAdapter) {
            postsLiveDataViewState = viewModel.getSubscribedPostsLiveDataViewState();
        } else {
            postsLiveDataViewState = viewModel.getAllPostsLiveDataViewState();
        }
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }

    @Override
    public RowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowSinglePostBinding rowSinglePostBinding =
                RowSinglePostBinding.inflate(hostFragment.getLayoutInflater(), parent, false);
        rowSinglePostBinding.setLifecycleOwner(hostFragment);

        return new RowHolder(rowSinglePostBinding);
    }

    @Override
    public void onBindViewHolder(RowHolder rowHolder, int position) {
        rowHolder.bindModel();
        strikethroughClickedPosts(rowHolder, position);
    }

    // region helper methods -----------------------------------------------------------------------

    public int getLastClickedRow() {
        return lastClickedRow;
    }

    public void setClickedPostIds(String[] clickedPostIds) {
        this.clickedPostIds = clickedPostIds;
    }

    public void strikethroughClickedPosts(RowHolder rowHolder, int position) {
        if (postsLiveDataViewState.getValue() != null) {
            if (Arrays.asList(clickedPostIds).contains(postsLiveDataViewState.getValue().postData.get(position).id)) {
                rowHolder.rowSinglePostBinding.title.setPaintFlags(rowHolder.rowSinglePostBinding.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                rowHolder.rowSinglePostBinding.title.setPaintFlags(rowHolder.rowSinglePostBinding.title.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    // endregion helper methods---------------------------------------------------------------------

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
            rowSinglePostBinding.setViewModel(viewModel);
            rowSinglePostBinding.executePendingBindings();
        }

        //placed here so data binding class can access it
        public LiveData<PostsViewState> getPostsLiveDataViewState() {
            return postsLiveDataViewState;
        }

        @Override
        public void onClick(View v) {
            int i = getAdapterPosition();
            boolean isSelfPost = getPostsLiveDataViewState().getValue().postData.get(i).isSelf;

            lastClickedRow = i;
            launchPostCallback.launchPost(i, isSelfPost, isSubscribedPostsAdapter);
        }
    }

    // endregion helper classes---------------------------------------------------------------------

    // region interfaces ---------------------------------------------------------------------------

    public interface launchPostCallback {
        void launchPost(int position, boolean isSelfPost, boolean isSubscribedPost);
    }

    // endregion interfaces ------------------------------------------------------------------------
}
