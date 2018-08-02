package com.aaronhalbert.meteorforreddit;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostHolder> {
    private static final String[] sItems = {
            "Never let this die",
            "Trump calls media 'very unpatriotic' for reporting on government affairs",
            "What a Dive!",
            "A bulldog, a pitbull and a rottweiler walked into a bar... And they were incredibly well behaved and loved by all :)",
            "Millennial's \"jokes\"",
            "ULPT: Want to make some fast money this summer? Go buy store bought brownies and take them to a music festival near you. Walk around and sell them for $20 each. Everybody will assume they’re pot brownies and by time they realize they aren’t you’ll be long gone.",
            "Anon teaches his office",
            "Smack dab in the middle of a 10AM-10PM shift yesterday...",
            "America in the 80s", "vel", "ligula", "vitae", "arcu", "aliquet", "mollis", "etiam",
            "vel", "erat", "placerat", "ante", "porttitor", "sodales", "pellentesque", "augue",
            "purus" };



    @Override
    public int getItemCount() {
        return sItems.length;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(com.aaronhalbert.meteorforreddit.R.layout.list_item_view, viewGroup, false);

        return new PostHolder(view);
    }

    public class PostHolder extends RecyclerView.ViewHolder {
        public TextView mTitle = null;

        public PostHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(com.aaronhalbert.meteorforreddit.R.id.title);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder postHolder, int i) {
        postHolder.mTitle.setText(sItems[i]);
    }
}
