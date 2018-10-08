package com.aaronhalbert.nosurfforreddit;

import android.text.Spanned;
import android.util.Log;

public class CommentsViewState {

    public int numComments;
    public Spanned[] commentBodies;
    public String[] commentDetails;

    public CommentsViewState(int numComments) {
        this.numComments = numComments;
        commentBodies = new Spanned[numComments];
        commentDetails = new String[numComments];
    }


}
