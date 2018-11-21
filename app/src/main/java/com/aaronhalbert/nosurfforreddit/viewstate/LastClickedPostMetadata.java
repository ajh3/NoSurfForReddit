package com.aaronhalbert.nosurfforreddit.viewstate;

/* data structure to hold info on the most-recently clicked post in a RecyclerView */

public class LastClickedPostMetadata {
    public final int lastClickedPostPosition;
    public final String lastClickedPostId;
    public final boolean lastClickedPostIsSelf;
    public final boolean lastClickedPostIsSubscribed;
    public final String lastClickedPostUrl;

    public LastClickedPostMetadata(int lastClickedPostPosition,
                                   String lastClickedPostId,
                                   boolean lastClickedPostIsSelf,
                                   String lastClickedPostUrl,
                                   boolean lastClickedPostIsSubscribed) {

        this.lastClickedPostPosition = lastClickedPostPosition;
        this.lastClickedPostId = lastClickedPostId;
        this.lastClickedPostIsSelf = lastClickedPostIsSelf;
        this.lastClickedPostUrl = lastClickedPostUrl;
        this.lastClickedPostIsSubscribed = lastClickedPostIsSubscribed;
    }
}
