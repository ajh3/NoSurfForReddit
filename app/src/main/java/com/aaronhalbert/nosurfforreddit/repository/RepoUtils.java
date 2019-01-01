package com.aaronhalbert.nosurfforreddit.repository;

import com.aaronhalbert.nosurfforreddit.room.ClickedPostId;

import java.util.List;

public class RepoUtils {
    public RepoUtils() { }

    /* works in conjunction with the mergeClickedPosts BiFunction. Sort of awkward. We first
     * transform a List of ClickedPostId objects into an array of Strings, and then the array back
     * into a List of Strings. */
    //TODO: Fix w/ AutoValue
    String[] getArrayOfClickedPostIds(List<ClickedPostId> input) {
        int size = input.size();
        String[] clickedPostIds = new String[size];

        for (int i = 0; i < size; i++) {
            clickedPostIds[i] = input.get(i).getClickedPostId();
        }

        return clickedPostIds;
    }
}
