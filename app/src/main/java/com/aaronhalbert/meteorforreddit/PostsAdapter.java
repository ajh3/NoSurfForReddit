package com.aaronhalbert.meteorforreddit;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostHolder> {

    private String[] mTitleArray = new String[25];

    PostsAdapter() {
        super();
    }

    public void setMTitleArray(String[] mTitleArray) {
        this.mTitleArray = mTitleArray;
    }

    @Override
    public int getItemCount() {
        return mTitleArray.length;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(com.aaronhalbert.meteorforreddit.R.layout.list_item_view, viewGroup, false);

        return new PostHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull PostHolder postHolder, int i) {
        postHolder.mTitle.setText(mTitleArray[i]);
    }



    public class PostHolder extends RecyclerView.ViewHolder {
        TextView mTitle = null;
        TextView mSubreddit = null;
        TextView mAuthor = null;


        public PostHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(com.aaronhalbert.meteorforreddit.R.id.title);
        }
    }

}
