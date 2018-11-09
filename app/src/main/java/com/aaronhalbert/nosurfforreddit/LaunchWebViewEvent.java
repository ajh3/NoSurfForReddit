package com.aaronhalbert.nosurfforreddit;

public class LaunchWebViewEvent {
    private String url;
    private String tag;
    private boolean doAnimation;

    public LaunchWebViewEvent(String url, String tag, boolean doAnimation) {
        this.url = url;
        this.tag = tag;
        this.doAnimation = doAnimation;
    }

    public String getUrl() {
        return url;
    }

    public String getTag() {
        return tag;
    }

    public boolean isDoAnimation() {
        return doAnimation;
    }
}
