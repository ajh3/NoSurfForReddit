package com.aaronhalbert.meteorforreddit;

public class RedditListingDataObject {
    private String modhash;
    private int dist;
    private RedditT3Object[] children = new RedditT3Object[dist];
    private String after;
    private String before;

    public String getModhash() {
        return modhash;
    }

    public int getDist() {
        return dist;
    }

    public RedditT3Object[] getChildren() {
        return children;
    }

    public String getAfter() {
        return after;
    }

    public String getBefore() {
        return before;
    }
}
