package com.aaronhalbert.nosurfforreddit.viewstate;

import android.text.Spanned;

import java.util.ArrayList;

public class PostsViewState {

    private static final int INITIAL_CAPACITY = 25;

    public ArrayList<PostDatum> postData = new ArrayList<>(INITIAL_CAPACITY);

    public static class PostDatum {
        public boolean isSelf;
        public int score;
        public int numComments;
        public String id;
        public String title;
        public String author;
        public String subreddit;
        public String url;
        public String thumbnailUrl;
        public String imageUrl;
        public String selfTextHtml;
    }
}
