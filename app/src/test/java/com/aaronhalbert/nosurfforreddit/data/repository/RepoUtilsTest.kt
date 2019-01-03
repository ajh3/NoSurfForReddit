package com.aaronhalbert.nosurfforreddit.data.repository

import com.aaronhalbert.nosurfforreddit.data.repository.network.RepoUtils
import com.aaronhalbert.nosurfforreddit.data.room.ClickedPostId
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is

class RepoUtilsTest {

    /* Note: RepoUtils is a trivially simple class, and this unit test is really just testing the
     * Kotlin standard library since the class doesn't contain any custom logic. But, since this
     * was the first unit test I ever wrote, I wanted to pick an extremely simple class to test. */
    private lateinit var sut: RepoUtils

    @Before
    fun setup() {
        sut = RepoUtils()
    }

    @Test
    fun convertListOfClickedPostIdsToListOfStrings_emptyList_emptyListReturned() {
        // Arrange
        val emptyList = emptyList<ClickedPostId>()
        // Act
        val result = sut.convertListOfClickedPostIdsToListOfStrings(emptyList)
        // Assert
        assertThat(result.size, Is(0))
    }

    @Test
    fun convertListOfClickedPostIdsToListOfStrings_singleItemList_sameStringReturned() {
        // Arrange
        val testId = "testId"
        val singleItem = ClickedPostId(testId)
        // Act
        val result = sut.convertListOfClickedPostIdsToListOfStrings(listOf(singleItem))
        // Assert
        assertThat(result.size, Is(1))
        assertThat(result[0], Is(testId))
    }
}
