package com.aaronhalbert.nosurfforreddit.viewstate;

public class LastClickedPostMetadata {
    private int lastClickedPostPosition;
    private String lastClickedPostId;
    private boolean lastClickedPostIsSelf;
    private boolean lastClickedPostIsSubscribed;

    public LastClickedPostMetadata(int lastClickedPostPosition,
                                   String lastClickedPostId,
                                   boolean lastClickedPostIsSelf,
                                   boolean lastClickedPostIsSubscribed) {

        this.lastClickedPostPosition = lastClickedPostPosition;
        this.lastClickedPostId = lastClickedPostId;
        this.lastClickedPostIsSelf = lastClickedPostIsSelf;
        this.lastClickedPostIsSubscribed = lastClickedPostIsSubscribed;
    }

    public int getLastClickedPostPosition() {
        return lastClickedPostPosition;
    }

    public String getLastClickedPostId() {
        return lastClickedPostId;
    }

    public boolean isLastClickedPostIsSelf() {
        return lastClickedPostIsSelf;
    }

    public boolean isLastClickedPostIsSubscribed() {
        return lastClickedPostIsSubscribed;
    }
}
