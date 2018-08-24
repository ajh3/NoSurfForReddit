package com.aaronhalbert.nosurfforreddit.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.reddit.RedditListingObject;
import com.squareup.picasso.Picasso;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostHolder> {

    private RedditListingObject currentRedditListingObject = null;
    private RecyclerViewToFragmentCallback recyclerViewToFragmentCallback;
    private Context context;

    public PostsAdapter(Context context, RecyclerViewToFragmentCallback recyclerViewToFragmentCallback) {
        //super(); is this needed or not?
        this.context = context;
        //this.recyclerViewToFragmentCallback = recyclerViewToFragmentCallback;
        this.recyclerViewToFragmentCallback = (PostsAdapter.RecyclerViewToFragmentCallback) recyclerViewToFragmentCallback;
    }

    public void setCurrentRedditListingObject(RedditListingObject currentRedditListingObject) {
        this.currentRedditListingObject = currentRedditListingObject;
    }

    @Override
    public int getItemCount() {
        if (currentRedditListingObject != null) {
            return currentRedditListingObject
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

        Picasso.get().load(getCurrentRedditListingObjectThumbnail(i))
                .fit()
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

        @Override
        public void onClick(View v) {
            recyclerViewToFragmentCallback.onItemClick(getCurrentRedditListingObjectUrl(getAdapterPosition()),
                    getCurrentRedditListingObjectIsSelf(getAdapterPosition()));

        }
    }

    //TODO make sure this is implemented where it needs to be
    public interface RecyclerViewToFragmentCallback {
        void onItemClick(String url, boolean isSelf);
    }



    private String getCurrentRedditListingObjectTitle(int i) {
        return currentRedditListingObject
                .getData()
                .getChildren()[i]
                .getData()
                .getTitle();
    }

    private String getCurrentRedditListingObjectSubreddit(int i) {
        return currentRedditListingObject
                .getData()
                .getChildren()[i]
                .getData()
                .getSubreddit();
    }

    private int getCurrentRedditListingObjectScore(int i) {
        return currentRedditListingObject
                .getData()
                .getChildren()[i]
                .getData()
                .getScore();
    }

    private String getCurrentRedditListingObjectThumbnail(int i) {
        return currentRedditListingObject
                .getData()
                .getChildren()[i]
                .getData()
                .getThumbnail();
    }

    private boolean getCurrentRedditListingObjectIsSelf(int i) {
        return currentRedditListingObject
                .getData()
                .getChildren()[i]
                .getData()
                .getIsSelf();
    }

    private String getCurrentRedditListingObjectUrl(int i) {
        return currentRedditListingObject
                .getData()
                .getChildren()[i]
                .getData()
                .getUrl();
    }

}
