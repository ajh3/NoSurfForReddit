package com.aaronhalbert.nosurfforreddit.viewstate;

import android.text.Spanned;

/* data structure to hold cleaned/transformed comment data from the Reddit API */

public class CommentsViewState {
    public final int numComments;
    public final Spanned[] commentBodies;
    public final String[] commentDetails;
    public final String id;

    public CommentsViewState(int numComments, String id) {
        this.numComments = numComments;
        commentBodies = new Spanned[numComments];
        commentDetails = new String[numComments];
        this.id = id;
    }
}
