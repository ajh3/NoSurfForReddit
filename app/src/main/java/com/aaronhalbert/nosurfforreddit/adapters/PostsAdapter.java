package com.aaronhalbert.nosurfforreddit.adapters;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.GlideApp;
import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.PostsViewState;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.databinding.RowSinglePostBinding;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.Arrays;


public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.RowHolder> {

    private RecyclerViewOnClickCallback recyclerViewOnClickCallback;
    private LoadListOfReadPostIds loadListOfReadPostIds; //TODO: rename to hostFragment or something like that
    private Context context; //TODO: convert to activity
    private Fragment hostFragment;
    NoSurfViewModel viewModel;
    private LiveData<PostsViewState> postsViewStateLiveData;
    private boolean isSubscribedPostsAdapter;

    public PostsAdapter(Context context, RecyclerViewOnClickCallback recyclerViewOnClickCallback, LoadListOfReadPostIds loadListOfReadPostIds, NoSurfViewModel viewModel, Fragment hostFragment, boolean isSubscribedPostsAdapter) {
        this.context = context;
        this.recyclerViewOnClickCallback = recyclerViewOnClickCallback;
        this.loadListOfReadPostIds = loadListOfReadPostIds;
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
        RowSinglePostBinding rowSinglePostBinding = RowSinglePostBinding.inflate(((Activity) context).getLayoutInflater(), parent, false);
        rowSinglePostBinding.setLifecycleOwner(hostFragment); //TODO: needed? seems to be working without

        return new RowHolder(rowSinglePostBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder rowHolder, int position) {
        rowHolder.bindModel(position);

        //TODO: this is crashing the app for some reason when current listing isn't being used????
        //String[] readPostIds = loadListOfReadPostIds.getReadPostIds();

        //TODO: getting null exception - how do I handle this? Can I bind this somehow?
        //if (Arrays.asList(readPostIds).contains(viewModel.getAllPostsLiveDataViewState().getValue().postData.get(position).id)) {
        //    rowHolder.rowSinglePostBinding.title.setPaintFlags(rowHolder.rowSinglePostBinding.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        //} else {
        //    rowHolder.rowSinglePostBinding.title.setPaintFlags(rowHolder.rowSinglePostBinding.title.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        //}
    }

    public class RowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final RowSinglePostBinding rowSinglePostBinding;
        int position;

        RowHolder(RowSinglePostBinding rowSinglePostBinding) {
            super(rowSinglePostBinding.getRoot());
            this.rowSinglePostBinding = rowSinglePostBinding;
            itemView.setOnClickListener(this);
        }

        void bindModel(int position) {
            this.position = position;
            rowSinglePostBinding.setController(this); // to call onClick
            rowSinglePostBinding.setViewModel(viewModel);
            rowSinglePostBinding.executePendingBindings();
        }

        @Override
        public void onClick(View v) {

            int i = getAdapterPosition();
            loadListOfReadPostIds.setLastClickedRow(i);

            //TODO: can I bind this somehow?
            //TODO fix this selfpost logic
            boolean isSelfPost = getPostsViewStateLiveData().getValue().postData.get(i).isSelf;

            if (isSelfPost) {
                recyclerViewOnClickCallback.launchPost(i, true, isSubscribedPostsAdapter); //TODO: these booleans are highly redundant...
            } else {
                recyclerViewOnClickCallback.launchPost(i, false, isSubscribedPostsAdapter);
            }
        }

        public LiveData<PostsViewState> getPostsViewStateLiveData() {
            return postsViewStateLiveData;
        }
    }

    //TODO make sure this is implemented where it needs to be
    //TODO these methods are doubled up with another interface that MainActivity is implementing
    public interface RecyclerViewOnClickCallback {
        void launchPost(int position, boolean isSelfPost, boolean isSubscribedPost);
    }

    public interface LoadListOfReadPostIds {
        String[] getReadPostIds();
        void setLastClickedRow(int lastClickedRow);
    }


}
