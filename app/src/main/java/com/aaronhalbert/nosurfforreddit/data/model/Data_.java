/*
 * Copyright (c) 2018-present, Aaron J. Halbert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.aaronhalbert.nosurfforreddit.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("ALL")
public class Data_ {
    @SerializedName("subreddit")
    @Expose
    private String subreddit;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;

    @SerializedName("score")
    @Expose
    private int score;

    @SerializedName("is_self")
    @Expose
    private boolean isSelf;

    @SerializedName("permalink")
    @Expose
    private String permalink;

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("selftext")
    @Expose
    private String selfText;

    @SerializedName("selftext_html")
    @Expose
    private String selfTextHtml;

    @SerializedName("preview")
    @Expose
    private Preview preview;

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("author")
    @Expose
    private String author;

    @SerializedName("body")
    @Expose
    private String body;

    @SerializedName("body_html")
    @Expose
    private String bodyHtml;

    @SerializedName("num_comments")
    @Expose
    private int numComments;

    @SerializedName("over_18")
    @Expose
    private boolean isNsfw;

    public String getSubreddit() {
        return subreddit;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public int getScore() {
        return score;
    }

    public boolean isIsSelf() {
        return isSelf;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getUrl() {
        return url;
    }

    public String getSelfText() {
        return selfText;
    }

    public String getSelfTextHtml() {
        return selfTextHtml;
    }

    public Preview getPreview() {
        return preview;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public int getNumComments() {
        return numComments;
    }

    public boolean isNsfw() {
        return isNsfw;
    }
}
