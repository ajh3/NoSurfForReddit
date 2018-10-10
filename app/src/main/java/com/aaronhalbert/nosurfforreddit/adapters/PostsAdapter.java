package com.aaronhalbert.nosurfforreddit.adapters;

import android.app.Activity;
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
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.databinding.RowSinglePostBinding;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.Arrays;


public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.RowHolder> {

    private Listing currentListing = null;
    private RecyclerViewOnClickCallback recyclerViewOnClickCallback;
    private LoadListOfReadPostIds loadListOfReadPostIds; //TODO: rename to hostFragment or something like that
    private Context context; //TODO: convert to activity
    private Fragment hostFragment;
    NoSurfViewModel viewModel;

    public PostsAdapter(Context context, RecyclerViewOnClickCallback recyclerViewOnClickCallback, LoadListOfReadPostIds loadListOfReadPostIds, NoSurfViewModel viewModel, Fragment hostFragment) {
        this.context = context;
        this.recyclerViewOnClickCallback = recyclerViewOnClickCallback;
        this.loadListOfReadPostIds = loadListOfReadPostIds;
        this.viewModel = viewModel;
        this.hostFragment = hostFragment;
    }

    public void setCurrentListing(Listing currentListing) { //TODO: pull this into the constructor instead
        this.currentListing = currentListing;
    }

    @Override
    public int getItemCount() {
        if (currentListing != null) {
            Log.e(getClass().toString(), "count is: " + currentListing.getData().getDist());
            return currentListing.getData().getDist();
        } else return 0;
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

        String[] readPostIds = loadListOfReadPostIds.getReadPostIds();

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




        //TODO the isSelf checking is a giant mess, should not be checking here and in MainActivity just to avoid crash on null imageUrl
        @Override
        public void onClick(View v) {

            int i = getAdapterPosition();
            loadListOfReadPostIds.setLastClickedRow(i);

            //TODO: can I bind this somehow?
            //TODO fix this selfpost logic
            boolean isSelfPost = viewModel.getAllPostsLiveDataViewState().getValue().postData.get(i).isSelf;

            if (isSelfPost) {
                recyclerViewOnClickCallback.launchSelfPost(i, true);
            } else {
                recyclerViewOnClickCallback.launchLinkPost(i, false);
            }
        }
    }




    //TODO make sure this is implemented where it needs to be
    //TODO these methods are doubled up with another interface that MainActivity is implementing
    public interface RecyclerViewOnClickCallback {
        void launchSelfPost(int position, boolean isSelfPost);
        void launchLinkPost(int position, boolean isSelfPost);
    }

    public interface LoadListOfReadPostIds {
        String[] getReadPostIds();
        void setLastClickedRow(int lastClickedRow);
    }


}
