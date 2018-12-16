package com.aaronhalbert.nosurfforreddit.viewstate;

/* data structure to hold info on the most-recently clicked post in a RecyclerView */

@SuppressWarnings("WeakerAccess")
public class LastClickedPostMetadata {
    public final int lastClickedPostPosition;
    public final String lastClickedPostId;
    public final boolean lastClickedPostIsSelf;
    public final boolean lastClickedPostIsSubscribed;
    public final String lastClickedPostUrl;
    public final String lastClickedPostPermalink;
    public final boolean lastClickedPostIsNsfw;
    public final String lastClickedPostTitle;
    public final String lastClickedPostSubreddit;

    public LastClickedPostMetadata(int lastClickedPostPosition,
                                   String lastClickedPostId,
                                   boolean lastClickedPostIsSelf,
                                   String lastClickedPostUrl,
                                   boolean lastClickedPostIsSubscribed,
                                   String lastClickedPostPermalink,
                                   boolean lastClickedPostIsNsfw,
                                   String lastClickedPostTitle,
                                   String lastClickedPostSubreddit) {

        this.lastClickedPostPosition = lastClickedPostPosition;
        this.lastClickedPostId = lastClickedPostId;
        this.lastClickedPostIsSelf = lastClickedPostIsSelf;
        this.lastClickedPostUrl = lastClickedPostUrl;
        this.lastClickedPostIsSubscribed = lastClickedPostIsSubscribed;
        this.lastClickedPostPermalink = lastClickedPostPermalink;
        this.lastClickedPostIsNsfw = lastClickedPostIsNsfw;
        this.lastClickedPostTitle = lastClickedPostTitle;
        this.lastClickedPostSubreddit = lastClickedPostSubreddit;
    }
}
