package com.aaronhalbert.nosurfforreddit;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostHolder> {

    private RedditListingObject currentRedditListingObject = null;

    PostsAdapter() {
        //super(); is this needed or not?
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
                .inflate(R.layout.list_item_view, viewGroup, false);

        return new PostHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull PostHolder postHolder, int i) {
        postHolder.mTitle.setText(currentRedditListingObject
                .getData()
                .getChildren()[i]
                .getData()
                .getTitle());
        postHolder.mSubreddit.setText("/r/" + currentRedditListingObject
                .getData()
                .getChildren()[i]
                .getData()
                .getSubreddit());
        postHolder.mScore.setText(" • " + String.valueOf(currentRedditListingObject
                .getData()
                .getChildren()[i]
                .getData()
                .getScore()));
    }

    class PostHolder extends RecyclerView.ViewHolder {
        TextView mTitle = null;
        TextView mSubreddit = null;
        TextView mScore = null;


        PostHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.title);
            mSubreddit = itemView.findViewById(R.id.subreddit);
            mScore = itemView.findViewById(R.id.score);
        }
    }

}
