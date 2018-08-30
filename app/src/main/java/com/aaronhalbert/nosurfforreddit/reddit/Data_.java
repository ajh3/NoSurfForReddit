
package com.aaronhalbert.nosurfforreddit.reddit;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data_ {

    @SerializedName("subreddit")
    @Expose
    private String subreddit;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;

    @SerializedName("score")
    @Expose
    private int score;

    @SerializedName("is_self")
    @Expose
    private boolean isSelf;

    @SerializedName("permalink")
    @Expose
    private String permalink;

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("selfText")
    @Expose
    private String selfText;

    @SerializedName("preview")
    @Expose
    private Preview preview;


    public String getSubreddit() {
        return subreddit;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public int getScore() {
        return score;
    }

    public boolean isIsSelf() {
        return isSelf;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getUrl() {
        return url;
    }

    public String getSelfText() {
        return selfText;
    }

    public Preview getPreview() {
        return preview;
    }
}
