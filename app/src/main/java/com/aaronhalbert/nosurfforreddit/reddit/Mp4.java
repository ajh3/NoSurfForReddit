
package com.aaronhalbert.nosurfforreddit.reddit;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Mp4 {

    @SerializedName("source")
    @Expose
    private Source__ source;
    @SerializedName("resolutions")
    @Expose
    private List<Resolution__> resolutions = null;

    public Source__ getSource() {
        return source;
    }

    public void setSource(Source__ source) {
        this.source = source;
    }

    public List<Resolution__> getResolutions() {
        return resolutions;
    }

    public void setResolutions(List<Resolution__> resolutions) {
        this.resolutions = resolutions;
    }

}
