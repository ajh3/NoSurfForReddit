package com.aaronhalbert.nosurfforreddit.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.GlideApp;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;


public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostHolder> {

    private Listing currentListing = null;
    private RecyclerViewOnClickCallback recyclerViewOnClickCallback;
    private Context context;

    public PostsAdapter(Context context, RecyclerViewOnClickCallback recyclerViewOnClickCallback) {
        this.context = context;
        this.recyclerViewOnClickCallback = (PostsAdapter.RecyclerViewOnClickCallback) recyclerViewOnClickCallback;
    }

    public void setCurrentListing(Listing currentListing) {
        this.currentListing = currentListing;
    }

    @Override
    public int getItemCount() {
        if (currentListing != null) {
            return currentListing
                    .getData()
                    .getDist();
        } else return 0;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_single_post, viewGroup, false);

        return new PostHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull PostHolder postHolder, int i) {
        postHolder.title.setText(getCurrentRedditListingObjectTitle(i));

        postHolder.subreddit.setText(context.getString(R.string.subreddit_abbreviation,
                getCurrentRedditListingObjectSubreddit(i)));

        postHolder.score.setText(context.getString(R.string.score,
                getCurrentRedditListingObjectScore(i)));

        GlideApp.with(context)
                .load(getCurrentRedditListingObjectThumbnail(i))
                .centerCrop()
                .placeholder(R.drawable.textposticon64)
                .into(postHolder.thumbnail);
    }

    class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title = null;
        TextView subreddit = null;
        TextView score = null;
        ImageView thumbnail = null;

        PostHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            subreddit = itemView.findViewById(R.id.subreddit);
            score = itemView.findViewById(R.id.score);
            thumbnail = itemView.findViewById(R.id.thumbnail);

            itemView.setOnClickListener(this);

        }

        //TODO the isSelf checking is a giant mess, should not be checking here and in MainActivity just to avoid crash on null imageUrl
        @Override
        public void onClick(View v) {

            int i = getAdapterPosition();

            if (getCurrentRedditListingObjectIsSelf(i)) {
                recyclerViewOnClickCallback.onItemClick(getCurrentRedditListingObjectUrl(i),
                        getCurrentRedditListingObjectIsSelf(i),
                        null,
                        getCurrentRedditListingObjectTitle(i),
                        getCurrentRedditListingObjectSelfText(i));
            } else {
                recyclerViewOnClickCallback.onItemClick(getCurrentRedditListingObjectUrl(i),
                        getCurrentRedditListingObjectIsSelf(i),
                        getCurrentRedditListingObjectImageUrl(i),
                        getCurrentRedditListingObjectTitle(i),
                        null);
            }


        }
    }

    //TODO make sure this is implemented where it needs to be
    public interface RecyclerViewOnClickCallback {
        void onItemClick(String url, boolean isSelf, String imageUrl, String title, String selfText);
    }


    private String getCurrentRedditListingObjectTitle(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getTitle();
    }

    private String getCurrentRedditListingObjectSubreddit(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getSubreddit();
    }

    private int getCurrentRedditListingObjectScore(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getScore();
    }

    private String getCurrentRedditListingObjectThumbnail(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getThumbnail();
    }

    private boolean getCurrentRedditListingObjectIsSelf(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .isIsSelf();
    }

    private String getCurrentRedditListingObjectUrl(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getUrl();
    }

    private String getCurrentRedditListingObjectImageUrl(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getPreview()
                .getImages()
                .get(0)
                .getSource()
                .getUrl();

    }

    private String getCurrentRedditListingObjectSelfText(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getSelfText();

    }

}
