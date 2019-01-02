package com.aaronhalbert.nosurfforreddit.repository

import com.aaronhalbert.nosurfforreddit.room.ClickedPostId

class RepoUtils {

    fun convertListOfClickedPostIdsToListOfStrings(input: List<ClickedPostId>): List<String> {

        return input.map { it.clickedPostId }
    }
}
