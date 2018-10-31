
package com.aaronhalbert.nosurfforreddit.network.redditschema;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("modhash")
    @Expose
    private String modhash;

    @SerializedName("dist")
    @Expose
    private int dist;

    @SerializedName("children")
    @Expose
    private List<Child> children = null;

    @SerializedName("after")
    @Expose
    private String after;

    @SerializedName("before")
    @Expose
    private String before;

    public String getModhash() {
        return modhash;
    }

    public int getDist() {
        return dist;
    }

    public List<Child> getChildren() {
        return children;
    }

    public String getAfter() {
        return after;
    }

    public String getBefore() {
        return before;
    }
}
