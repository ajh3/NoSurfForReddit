
package com.aaronhalbert.nosurfforreddit.redditschema;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Preview {

    @SerializedName("images")
    @Expose
    private List<Image> images = null;
    @SerializedName("enabled")
    @Expose
    private boolean enabled;


    public List<Image> getImages() {
        return images;
    }


    public boolean isEnabled() {
        return enabled;
    }



}
