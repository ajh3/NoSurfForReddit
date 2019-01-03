package com.aaronhalbert.nosurfforreddit.data.network

import com.aaronhalbert.nosurfforreddit.data.room.ClickedPostId

class RepoUtils {

    fun convertListOfClickedPostIdsToListOfStrings(input: List<ClickedPostId>): List<String> {

        return input.map { it.clickedPostId }
    }
}
