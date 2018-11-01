package com.aaronhalbert.nosurfforreddit.viewstate;

import android.text.Spanned;

public class CommentsViewState {

    public final int numComments;
    public final Spanned[] commentBodies;
    public final String[] commentDetails;

    public CommentsViewState(int numComments) {
        this.numComments = numComments;
        commentBodies = new Spanned[numComments];
        commentDetails = new String[numComments];
    }
}
