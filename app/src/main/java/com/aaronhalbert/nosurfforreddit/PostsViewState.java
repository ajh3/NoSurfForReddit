package com.aaronhalbert.nosurfforreddit;

import android.text.Spanned;

import java.util.ArrayList;

public class PostsViewState {

    public ArrayList<PostDatum> postData = new ArrayList<>(25);

    public PostsViewState() {

    }

    public static class PostDatum {
        public boolean isSelf;

        public String id;
        public String title;
        public String author;
        public String subreddit;
        public int score;
        public int numComments;

        public String url;
        public String thumbnailUrl;
        public String imageUrl;

        public String selfTextHtml;
    }
}
