package com.aaronhalbert.nonaddictivereddit4droid;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostHolder> {
    private static final String[] items = { "lorem", "ipsum", "dolor",
        "sit", "amet", "consectetuer", "adipiscing", "elit", "morbi", "vel",
        "ligula", "vitae", "arcu", "aliquet", "mollis", "etiam", "vel", "erat",
        "placerat", "ante", "porttitor", "sodales", "pellentesque", "augue", "purus" };

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = new TextView(viewGroup.getContext());

        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder postHolder, int i) {
        postHolder.textView.setText(items[i]);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }


    public class PostHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public PostHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
