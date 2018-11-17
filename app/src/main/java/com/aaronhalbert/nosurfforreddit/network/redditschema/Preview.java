
package com.aaronhalbert.nosurfforreddit.network.redditschema;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("ALL")
public class Preview {
    @SerializedName("images")
    @Expose
    private final List<Image> images = null;

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
