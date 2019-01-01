package com.aaronhalbert.nosurfforreddit.repository

import com.aaronhalbert.nosurfforreddit.room.ClickedPostId

class RepoUtils {

    /* works in conjunction with the mergeClickedPosts BiFunction in MainActivityViewModel. */
    //TODO: Fix w/ AutoValue
    //TODO: Should be able to modify combinePostsWithClickedPostIds to take a List instead of an Array?
    fun getArrayOfClickedPostIds(input: List<ClickedPostId>): Array<String> {
        val list = mutableListOf<String>()

        input.forEach { list.add(it.clickedPostId) }

        return list.toTypedArray()
    }
}
