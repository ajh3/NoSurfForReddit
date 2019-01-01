package com.aaronhalbert.nosurfforreddit.repository

import com.aaronhalbert.nosurfforreddit.room.ClickedPostId
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.hamcrest.CoreMatchers.`is` as Is

class RepoUtilsTest {

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

    @Test
    fun convertListOfClickedPostIdsToListOfStrings_multipleItemList_sameStringsReturned() {

        // Arrange
        val testId1 = "testId"
        val testId2 = "testId2"
        val item1 = ClickedPostId(testId1)
        val item2 = ClickedPostId(testId2)

        // Act
        val result = sut.convertListOfClickedPostIdsToListOfStrings(listOf(item1, item2))

        // Assert
        assertThat(result.size, Is(2))
        assertThat(result[0], Is(testId1))
        assertThat(result[1], Is(testId2))
    }
}
