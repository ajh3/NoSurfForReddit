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

import android.text.Spanned;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.core.text.HtmlCompat;

@SuppressWarnings("ALL")
public class Listing {
    private static final String AUTO_MODERATOR = "AutoModerator";
    private static final String USER_ABBREVIATION = "u/";
    private static final String BULLET_POINT = " \u2022 ";
    public static final String DEFAULT = "default";
    public static final String SELF = "self";
    public static final String NSFW = "nsfw";
    public static final String IMAGE = "image";
    public static final String SPOILER = "spoiler";

    @SerializedName("kind")
    @Expose
    private String kind;

    @SerializedName("data")
    @Expose
    private Data data;

    public String getKind() {
        return kind;
    }

    public Data getData() {
        return data;
    }

    // region helper methods used by repository ----------------------------------------------------

    /* These are mostly data cleaning routines that get applied against the raw data from
     * the Reddit API
     *
     * tuck them here instead of in repository to keep it tidy */

    public String getCommentAuthor(int i) {
        return getData()
                .getChildren()
                .get(i)
                .getData()
                .getAuthor();
    }

    public int getCommentScore(int autoModOffset, int i) {
        return getData()
                .getChildren()
                .get(autoModOffset + i)
                .getData()
                .getScore();
    }

    public String getCommentId() {
        return getData()
                .getChildren()
                .get(0)
                .getData()
                .getId();
    }

    public String pickImageUrl(int i) {
        Data_ data = getData().getChildren().get(i).getData();

        if (data.getPreview() == null) {
            return DEFAULT;
        } else {
            String encodedImageUrl = data
                    .getPreview()
                    .getImages()
                    .get(0)
                    .getSource()
                    .getUrl();
            return encodedImageUrl;
        }
    }

    public int getNumTopLevelComments() {
        return getData().getChildren().size();
    }

    public boolean isFirstCommentByAutoMod() {
        return getCommentAuthor(0).equals(AUTO_MODERATOR);
    }

    public int calculateAutoModOffset() {
        if (isFirstCommentByAutoMod()) {
            return 1;
        } else {
            return 0;
        }
    }

    public String getCommentBodyHtml(int autoModOffset, int i) {
        return getData()
                .getChildren()
                .get(autoModOffset + i)
                .getData()
                .getBodyHtml();
    }

    // Reddit API provides twice-encoded HTML... ¯\_(ツ)_/¯
    public String formatSelfPostSelfTextHtml(String twiceEncodedSelfTextHtml) {
        if ((twiceEncodedSelfTextHtml != null) && (!"".equals(twiceEncodedSelfTextHtml))) {
            String onceEncodedSelfTextHtml = decodeHtml(twiceEncodedSelfTextHtml).toString();
            String decodedSelfTextHtml = decodeHtml(onceEncodedSelfTextHtml).toString();
            return (String) trimTrailingWhitespace(decodedSelfTextHtml);
        } else {
            return "";
        }
    }

    public Spanned formatCommentBodyHtml(int autoModOffset, int i) {
        String unescaped = decodeHtml(getCommentBodyHtml(autoModOffset, i)).toString();
        Spanned escaped = decodeHtml(unescaped);

        return (Spanned) trimTrailingWhitespace(escaped);
    }

    public String pickThumbnailUrl(String encodedThumbnailUrl) {
        String thumbnailUrl;

        switch (encodedThumbnailUrl) {
            case DEFAULT:
                thumbnailUrl = DEFAULT;
                break;
            case SELF:
                thumbnailUrl = SELF;
                break;
            case NSFW:
                thumbnailUrl = NSFW;
                break;
            case IMAGE:
                thumbnailUrl = IMAGE;
                break;
            case SPOILER:
                thumbnailUrl = SPOILER;
                break;
            default:
                thumbnailUrl = decodeHtml(encodedThumbnailUrl).toString();
                break;
        }
        return thumbnailUrl;
    }

    public Spanned decodeHtml(String encoded) {
        return HtmlCompat.fromHtml(encoded, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    public CharSequence trimTrailingWhitespace(CharSequence source) {
        if (source == null) return "";

        int i = source.length();

        //decrement i and check if that character is whitespace
        do { --i; } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        //tick i up by 1 to return the full non-whitespace sequence
        return source.subSequence(0, i+1);
    }

    public String formatCommentDetails(String commentAuthor, int commentScore) {
        return USER_ABBREVIATION
                + commentAuthor
                + BULLET_POINT
                + Integer.toString(commentScore);
    }

    // region helper methods used by repository ----------------------------------------------------
}
