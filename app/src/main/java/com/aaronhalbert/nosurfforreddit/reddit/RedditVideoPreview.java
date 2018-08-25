
package com.aaronhalbert.nosurfforreddit.reddit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RedditVideoPreview {

    @SerializedName("fallback_url")
    @Expose
    private String fallbackUrl;
    @SerializedName("height")
    @Expose
    private int height;
    @SerializedName("width")
    @Expose
    private int width;
    @SerializedName("scrubber_media_url")
    @Expose
    private String scrubberMediaUrl;
    @SerializedName("dash_url")
    @Expose
    private String dashUrl;
    @SerializedName("duration")
    @Expose
    private int duration;
    @SerializedName("hls_url")
    @Expose
    private String hlsUrl;
    @SerializedName("is_gif")
    @Expose
    private boolean isGif;
    @SerializedName("transcoding_status")
    @Expose
    private String transcodingStatus;

    public String getFallbackUrl() {
        return fallbackUrl;
    }

    public void setFallbackUrl(String fallbackUrl) {
        this.fallbackUrl = fallbackUrl;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getScrubberMediaUrl() {
        return scrubberMediaUrl;
    }

    public void setScrubberMediaUrl(String scrubberMediaUrl) {
        this.scrubberMediaUrl = scrubberMediaUrl;
    }

    public String getDashUrl() {
        return dashUrl;
    }

    public void setDashUrl(String dashUrl) {
        this.dashUrl = dashUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getHlsUrl() {
        return hlsUrl;
    }

    public void setHlsUrl(String hlsUrl) {
        this.hlsUrl = hlsUrl;
    }

    public boolean isIsGif() {
        return isGif;
    }

    public void setIsGif(boolean isGif) {
        this.isGif = isGif;
    }

    public String getTranscodingStatus() {
        return transcodingStatus;
    }

    public void setTranscodingStatus(String transcodingStatus) {
        this.transcodingStatus = transcodingStatus;
    }

}
