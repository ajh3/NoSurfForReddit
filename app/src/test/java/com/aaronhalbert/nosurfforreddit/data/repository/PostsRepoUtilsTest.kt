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

package com.aaronhalbert.nosurfforreddit.data.repository

import com.aaronhalbert.nosurfforreddit.data.remote.posts.PostsRepoUtils
import com.aaronhalbert.nosurfforreddit.data.local.clickedpostids.model.ClickedPostId
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is

class PostsRepoUtilsTest {

    /* Note: PostsRepoUtils is a trivially simple class, and this unit test is really just testing the
     * Kotlin standard library since the class doesn't contain any custom logic. But, since this
     * was the first unit test I ever wrote, I wanted to pick an extremely simple class to test. */
    private lateinit var underTest: PostsRepoUtils

    @Before
    fun setup() {
        underTest = PostsRepoUtils()
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
        val singleItem =
            ClickedPostId(testId)
        // Act
        val result = underTest.convertListOfClickedPostIdsToListOfStrings(listOf(singleItem))
        // Assert
        assertThat(result.size, Is(1))
        assertThat(result[0], Is(testId))
    }
}
