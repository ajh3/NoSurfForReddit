package com.aaronhalbert.nosurfforreddit.network.redditschema;

import android.text.Spanned;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.core.text.HtmlCompat;

@SuppressWarnings("ALL")
public class Listing {
    private static final String AUTO_MODERATOR = "AutoModerator";
    private static final String LINK_POST_DEFAULT_THUMBNAIL = "android.resource://com.aaronhalbert.nosurfforreddit/drawable/link_post_default_thumbnail_192";
    private static final String SELF_POST_DEFAULT_THUMBNAIL = "android.resource://com.aaronhalbert.nosurfforreddit/drawable/self_post_default_thumbnail_192";
    private static final String LINK_POST_NSFW_THUMBNAIL = "android.resource://com.aaronhalbert.nosurfforreddit/drawable/link_post_nsfw_thumbnail_192";
    private static final String DEFAULT = "default";
    private static final String SELF = "self";
    private static final String NSFW = "nsfw";
    private static final String IMAGE = "image";
    private static final String USER_ABBREVIATION = "u/";
    private static final String BULLET_POINT = " \u2022 ";
    private static final String SPOILER = "spoiler";

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
            return LINK_POST_DEFAULT_THUMBNAIL;
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
                thumbnailUrl = LINK_POST_DEFAULT_THUMBNAIL;
                break;
            case SELF:
                thumbnailUrl = SELF_POST_DEFAULT_THUMBNAIL;
                break;
            case NSFW:
                thumbnailUrl = LINK_POST_NSFW_THUMBNAIL;
                break;
            case IMAGE:
                thumbnailUrl = LINK_POST_DEFAULT_THUMBNAIL;
                break;
            case SPOILER:
                thumbnailUrl = LINK_POST_NSFW_THUMBNAIL;
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
