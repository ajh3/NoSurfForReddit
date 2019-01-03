package com.aaronhalbert.nosurfforreddit.repository

import android.content.Intent
import android.net.Uri
import com.nhaarman.mockitokotlin2.KStubbing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is

const val ACCESS_DENIED_ERROR_CODE = "access_denied"

class AuthenticatorUtilsTest {
    private lateinit var sut: AuthenticatorUtils
    private val dummyCode = "dummyCode"

    @Before
    fun setup() {
        sut = AuthenticatorUtils()
    }

    @Test
    fun buildAuthUrl_success_stringMatches() {
        //TODO: move buildAuthUrl() out of AuthenticatorUtils, so the class can be fully tested?
        /* this function comprises only static calls to classes I don't own - no reason to test,
         * as if I wrap the static calls in wrapper classes and mock them, I'll just be testing
         * the mocks. */
    }

    @Test
    fun extractCodeFromIntent_intentHasValidData_success_validCodeReturned() {
        // Arrange
        val mockUri : Uri = mock {
            errorIsEmpty()
            codeIsDummyCode()
        }
        val mockIntent : Intent = intentDataIsMockUri(mockUri)
        // Act
        val result = sut.extractCodeFromIntent(mockIntent)
        // Assert
        assertThat(result, Is(dummyCode))
    }

    @Test
    fun extractCodeFromIntent_intentDataIsNull_failure_emptyCodeReturned() {
        // Arrange
        val mockIntent : Intent = intentDataIsNull()
        // Act
        val result = sut.extractCodeFromIntent(mockIntent)
        // Assert
        assertIsEmptyString(result)
    }

    @Test
    fun extractCodeFromIntent_codeIsNull_failure_emptyCodeReturned() {
        // Arrange
        val mockUri : Uri = mock {
            errorIsEmpty()
            codeIsNull()
        }
        val mockIntent : Intent = intentDataIsMockUri(mockUri)
        // Act
        val result = sut.extractCodeFromIntent(mockIntent)
        // Assert
        assertIsEmptyString(result)
    }

    @Test
    fun extractCodeFromIntent_codeIsEmpty_failure_emptyCodeReturned() {
        // Arrange
        val mockUri : Uri = mock {
            errorIsEmpty()
            codeIsEmpty()
        }
        val mockIntent : Intent = intentDataIsMockUri(mockUri)
        // Act
        val result = sut.extractCodeFromIntent(mockIntent)
        // Assert
        assertIsEmptyString(result)
    }

    @Test
    fun extractCodeFromIntent_errorIsAccessDenied_failure_emptyCodeReturned() {
        // Arrange
        val mockUri : Uri = mock {
            errorIsAccessDenied()
            codeIsDummyCode()
        }
        val mockIntent : Intent = intentDataIsMockUri(mockUri)
        // Act
        val result = sut.extractCodeFromIntent(mockIntent)
        // Assert
        assertIsEmptyString(result)
    }

    // region helper methods -----------------------------------------------------------------------

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
