
package com.aaronhalbert.nosurfforreddit.network.redditschema;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("ALL")
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

    @SerializedName("selftext")
    @Expose
    private String selfText;

    @SerializedName("selftext_html")
    @Expose
    private String selfTextHtml;

    @SerializedName("preview")
    @Expose
    private Preview preview;

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("author")
    @Expose
    private String author;

    @SerializedName("body")
    @Expose
    private String body;

    @SerializedName("body_html")
    @Expose
    private String bodyHtml;

    @SerializedName("num_comments")
    @Expose
    private int numComments;

    @SerializedName("over_18")
    @Expose
    private boolean isNsfw;

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

    public String getSelfTextHtml() {
        return selfTextHtml;
    }

    public Preview getPreview() {
        return preview;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public int getNumComments() {
        return numComments;
    }

    public boolean isNsfw() {
        return isNsfw;
    }
}
