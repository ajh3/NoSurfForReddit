package com.aaronhalbert.nosurfforreddit.webview;

public class LaunchWebViewParams {
    public final String url;
    public final String tag;
    public final boolean doAnimation;

    public LaunchWebViewParams(String url, String tag, boolean doAnimation) {
        this.url = url;
        this.tag = tag;
        this.doAnimation = doAnimation;
    }
}
