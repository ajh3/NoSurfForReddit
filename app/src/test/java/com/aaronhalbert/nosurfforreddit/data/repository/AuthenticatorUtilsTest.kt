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

import android.content.Intent
import android.net.Uri
import com.aaronhalbert.nosurfforreddit.data.remote.auth.AuthenticatorUtils
import com.aaronhalbert.nosurfforreddit.data.remote.auth.CODE
import com.aaronhalbert.nosurfforreddit.data.remote.auth.ERROR
import com.nhaarman.mockitokotlin2.KStubbing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is

private const val ACCESS_DENIED_ERROR_CODE = "access_denied"

class AuthenticatorUtilsTest {
    private lateinit var underTest: AuthenticatorUtils
    private val dummyCode = "dummyCode"

    @Before
    fun setup() {
        underTest = AuthenticatorUtils()
    }

    /* note that buildAuthUrl() comprises no logic of my own, it's just calls to 3rd-party static
     * methods. So, this test is effectively testing code that is not my own. Normally this would
     * be undesirable, but in this case, this test is still useful because it'll warn us if any
     * of the constants used to build the URL accidentally get changed.
     *
     * Also note that this test works by asserting the beginning and end substrings of the URL:
     * what's in the middle is simply a randomly-generated UUID, which there is no need to test -
     * we can just assume that UUID.randomUUID() works. */
    @Test
    fun `buildAuthUrl() - ensure that none of the URL's component constants have accidentally changed`() {
        val result = underTest.buildAuthUrl()

        assertThat(
            result,
            startsWith("https://www.reddit.com/api/v1/authorize.compact?client_id=jPF59UF5MbMkWg&response_type=code&state=")
        )
        assertThat(
            result,
            endsWith("&redirect_uri=nosurfforreddit%3A%2F%2Foauth&duration=permanent&scope=identity%20mysubreddits%20read")
        )
    }

    @Test
    fun `extractCodeFromIntent() - valid string returned if the intent has a valid URI`() {
        // Arrange
        val mockUri: Uri = mock {
            errorIsEmpty()
            codeIsDummyCode()
        }
        val mockIntent: Intent = intentDataIsMockUri(mockUri)
        // Act
        val result = underTest.extractCodeFromIntent(mockIntent)
        // Assert
        assertThat(result, Is(dummyCode))
    }

    @Test
    fun `extractCodeFromIntent() - empty string returned if the intent has a null URI`() {
        // Arrange
        val mockIntent: Intent = intentDataIsNull()
        // Act
        val result = underTest.extractCodeFromIntent(mockIntent)
        // Assert
        assertIsEmptyString(result)
    }

    @Test
    fun `extractCodeFromIntent() - empty string returned if code is null`() {
        // Arrange
        val mockUri: Uri = mock {
            errorIsEmpty()
            codeIsNull()
        }
        val mockIntent: Intent = intentDataIsMockUri(mockUri)
        // Act
        val result = underTest.extractCodeFromIntent(mockIntent)
        // Assert
        assertIsEmptyString(result)
    }

    @Test
    fun `extractCodeFromIntent() - empty string returned if code is empty`() {
        // Arrange
        val mockUri: Uri = mock {
            errorIsEmpty()
            codeIsEmpty()
        }
        val mockIntent: Intent = intentDataIsMockUri(mockUri)
        // Act
        val result = underTest.extractCodeFromIntent(mockIntent)
        // Assert
        assertIsEmptyString(result)
    }

    @Test
    fun `extractCodeFromIntent() - empty string returned if there is an access denied error`() {
        // Arrange
        val mockUri: Uri = mock {
            errorIsAccessDenied()
            codeIsDummyCode()
        }
        val mockIntent: Intent = intentDataIsMockUri(mockUri)
        // Act
        val result = underTest.extractCodeFromIntent(mockIntent)
        // Assert
        assertIsEmptyString(result)
    }

    // region helper methods -----------------------------------------------------------------------

    /* importing org.hamcrest.text.IsEmptyString inexplicably breaks Android Studio, so I have
     * to write this custom function ¯\_(ツ)_/¯ */
    private fun assertIsEmptyString(result: String) {
        assertThat(result, Is(""))
    }

    private fun KStubbing<Uri>.errorIsEmpty() {
        on { getQueryParameter(ERROR) } doReturn ""
    }

    private fun KStubbing<Uri>.errorIsAccessDenied() {
        on { getQueryParameter(ERROR) } doReturn ACCESS_DENIED_ERROR_CODE
    }

    private fun KStubbing<Uri>.codeIsEmpty() {
        on { getQueryParameter(CODE) } doReturn ""
    }

    private fun KStubbing<Uri>.codeIsDummyCode() {
        on { getQueryParameter(CODE) } doReturn dummyCode
    }

    private fun KStubbing<Uri>.codeIsNull() {
        on { getQueryParameter(CODE) } doReturn null
    }

    private fun intentDataIsMockUri(mockUri: Uri): Intent =
        mock { on { data } doReturn mockUri }

    private fun intentDataIsNull(): Intent = mock { on { data } doReturn null }

    // endregion helper methods --------------------------------------------------------------------
}
