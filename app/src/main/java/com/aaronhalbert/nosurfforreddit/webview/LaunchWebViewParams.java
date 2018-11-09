package com.aaronhalbert.nosurfforreddit.webview;

public class LaunchWebViewParams {
    private String url;
    private String tag;
    private boolean doAnimation;

    public LaunchWebViewParams(String url, String tag, boolean doAnimation) {
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
