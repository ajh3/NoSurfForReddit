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

    public LastClickedPostMetadata(int lastClickedPostPosition,
                                   String lastClickedPostId,
                                   boolean lastClickedPostIsSelf,
                                   String lastClickedPostUrl,
                                   boolean lastClickedPostIsSubscribed,
                                   String lastClickedPostPermalink) {

        this.lastClickedPostPosition = lastClickedPostPosition;
        this.lastClickedPostId = lastClickedPostId;
        this.lastClickedPostIsSelf = lastClickedPostIsSelf;
        this.lastClickedPostUrl = lastClickedPostUrl;
        this.lastClickedPostIsSubscribed = lastClickedPostIsSubscribed;
        this.lastClickedPostPermalink = lastClickedPostPermalink;
    }
}
