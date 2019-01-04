package com.aaronhalbert.nosurfforreddit.data.repository

import com.aaronhalbert.nosurfforreddit.data.network.RepoUtils
import com.aaronhalbert.nosurfforreddit.data.room.ClickedPostId
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is

class RepoUtilsTest {

    /* Note: RepoUtils is a trivially simple class, and this unit test is really just testing the
     * Kotlin standard library since the class doesn't contain any custom logic. But, since this
     * was the first unit test I ever wrote, I wanted to pick an extremely simple class to test. */
    private lateinit var underTest: RepoUtils

    @Before
    fun setup() {
        underTest = RepoUtils()
    }

    @Test
    fun `convertListOfClickedPostIdsToListOfStrings() - empty input produces empty output`() {
        // Arrange
        val emptyList = emptyList<ClickedPostId>()
        // Act
        val result = underTest.convertListOfClickedPostIdsToListOfStrings(emptyList)
        // Assert
        assertThat(result.size, Is(0))
    }

    @Test
    fun `convertListOfClickedPostIdsToListOfStrings() - output contains same string as input and is of same size`() {
        // Arrange
        val testId = "testId"
        val singleItem = ClickedPostId(testId)
        // Act
        val result = underTest.convertListOfClickedPostIdsToListOfStrings(listOf(singleItem))
        // Assert
        assertThat(result.size, Is(1))
        assertThat(result[0], Is(testId))
    }
}
