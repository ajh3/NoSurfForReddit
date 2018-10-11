package com.aaronhalbert.nosurfforreddit.adapters;

import android.arch.lifecycle.LiveData;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.PostsViewState;
import com.aaronhalbert.nosurfforreddit.databinding.RowSinglePostBinding;

import java.util.Arrays;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.RowHolder> {
    private boolean isSubscribedPostsAdapter;
    private int lastClickedRow;
    private String[] readPostIds = new String[0]; //initialize to empty array in case it is read from before written to by Observer
    private Fragment hostFragment;
    private LiveData<PostsViewState> postsViewStateLiveData;
    private NoSurfViewModel viewModel;
    private launchPostCallback launchPostCallback;

    public PostsAdapter(launchPostCallback launchPostCallback, NoSurfViewModel viewModel, Fragment hostFragment, boolean isSubscribedPostsAdapter) {
        this.launchPostCallback = launchPostCallback;
        this.viewModel = viewModel;
        this.hostFragment = hostFragment;
        this.isSubscribedPostsAdapter = isSubscribedPostsAdapter;

        if (isSubscribedPostsAdapter) {
            postsViewStateLiveData = viewModel.getHomePostsLiveDataViewState();
        } else {
            postsViewStateLiveData = viewModel.getAllPostsLiveDataViewState();
        }
    }

    @Override
    public int getItemCount() {
        return 25;
    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowSinglePostBinding rowSinglePostBinding = RowSinglePostBinding.inflate(hostFragment.getLayoutInflater(), parent, false);
        rowSinglePostBinding.setLifecycleOwner(hostFragment); //TODO: needed? seems to work without

        return new RowHolder(rowSinglePostBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder rowHolder, int position) {
        rowHolder.bindModel();
        strikethroughReadPosts(rowHolder, position);
    }

    private void strikethroughReadPosts(RowHolder rowHolder, int position) {
        //TODO: Instead of using getValue, can I bind this somehow, or should I use an observer?
        if (postsViewStateLiveData.getValue() != null) {
            if (Arrays.asList(readPostIds).contains(postsViewStateLiveData.getValue().postData.get(position).id)) {
                rowHolder.rowSinglePostBinding.title.setPaintFlags(rowHolder.rowSinglePostBinding.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                rowHolder.rowSinglePostBinding.title.setPaintFlags(rowHolder.rowSinglePostBinding.title.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    public int getLastClickedRow() {
        return lastClickedRow;
    }

    public void setReadPostIds(String[] readPostIds) {
        this.readPostIds = readPostIds;
    }

    public class RowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final RowSinglePostBinding rowSinglePostBinding;

        RowHolder(RowSinglePostBinding rowSinglePostBinding) {
            super(rowSinglePostBinding.getRoot());
            this.rowSinglePostBinding = rowSinglePostBinding;
            itemView.setOnClickListener(this);
        }

        void bindModel() {
            rowSinglePostBinding.setController(this); // to call onClick
            rowSinglePostBinding.setViewModel(viewModel);
            rowSinglePostBinding.executePendingBindings();
        }

        @Override
        public void onClick(View v) {
            int i = getAdapterPosition();
            lastClickedRow = i;
            boolean isSelfPost = getPostsViewStateLiveData().getValue().postData.get(i).isSelf;

            launchPostCallback.launchPost(i, isSelfPost, isSubscribedPostsAdapter);
        }

        //placed in RowHolder class so data binding class can access it
        public LiveData<PostsViewState> getPostsViewStateLiveData() {
            return postsViewStateLiveData;
        }
    }

    public interface launchPostCallback {
        void launchPost(int position, boolean isSelfPost, boolean isSubscribedPost);
    }
}
