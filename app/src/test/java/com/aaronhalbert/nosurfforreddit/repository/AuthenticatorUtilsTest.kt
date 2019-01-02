package com.aaronhalbert.nosurfforreddit.repository

import android.content.Intent
import android.net.Uri
import com.aaronhalbert.nosurfforreddit.BuildConfig
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is

const val AUTH_URL_RESPONSE_TYPE = "&response_type="
const val RESPONSE_TYPE = "code"
const val AUTH_URL_STATE = "&state="
const val AUTH_URL_REDIRECT_URI = "&redirect_uri="
const val AUTH_URL_DURATION = "&duration="
const val DURATION = "permanent"
const val AUTH_URL_SCOPE = "&scope="
const val SCOPE = "identity mysubreddits read"

const val ACCESS_DENIED_ERROR_CODE = "access_denied"

class AuthenticatorUtilsTest {
    private lateinit var sut: AuthenticatorUtils
    private val fakeUUID = "fakeUUID"
    private val mockCode = "mockCode"

    @Before
    fun setup() {
        val mockRandomUUIDWrapper : RandomUUIDWrapper = mock {
            on { randomUUID() } doReturn fakeUUID
        }

        sut = AuthenticatorUtils(mockRandomUUIDWrapper)
    }

    @Test
    fun buildAuthUrl_success() {
        // Arrange - N/A
        // Act
        val result = sut.buildAuthUrl()
        // Assert
        assertThat(result, Is(BuildConfig.AUTH_URL_BASE
        + BuildConfig.CLIENT_ID
        + AUTH_URL_RESPONSE_TYPE
        + RESPONSE_TYPE
        + AUTH_URL_STATE
        + fakeUUID
        + AUTH_URL_REDIRECT_URI
        + BuildConfig.REDIRECT_URI
        + AUTH_URL_DURATION
        + DURATION
        + AUTH_URL_SCOPE
        + SCOPE))
    }

    @Test
    fun extractCodeFromIntent_intentHasValidData_validCodeReturned() {
        // Arrange
        val mockUri : Uri = mock {
            on { getQueryParameter(ERROR) } doReturn ""
            on { getQueryParameter(CODE) } doReturn mockCode
        }
        val mockIntent : Intent = mock { on { data } doReturn mockUri }
        // Act
        val result = sut.extractCodeFromIntent(mockIntent)
        // Assert
        assertThat(result, Is(mockCode))
    }

    @Test
    fun extractCodeFromIntent_intentDataIsNull_emptyStringReturned() {
        // Arrange
        val mockIntent : Intent = mock { on { data } doReturn null }
        // Act
        val result = sut.extractCodeFromIntent(mockIntent)
        // Assert
        assertThat(result, Is(""))
    }

    @Test
    fun extractCodeFromIntent_codeIsNull_emptyStringReturned() {
        // Arrange
        val mockUri : Uri = mock {
            on { getQueryParameter(ERROR) } doReturn ""
            on { getQueryParameter(CODE) } doReturn null
        }
        val mockIntent : Intent = mock { on { data } doReturn mockUri }
        // Act
        val result = sut.extractCodeFromIntent(mockIntent)
        // Assert
        assertThat(result, Is(""))
    }

    @Test
    fun extractCodeFromIntent_codeIsEmpty_emptyStringReturned() {
        // Arrange
        val mockUri : Uri = mock {
            on { getQueryParameter(ERROR) } doReturn ""
            on { getQueryParameter(CODE) } doReturn ""
        }
        val mockIntent : Intent = mock { on { data } doReturn mockUri }
        // Act
        val result = sut.extractCodeFromIntent(mockIntent)
        // Assert
        assertThat(result, Is(""))
    }

    @Test
    fun extractCodeFromIntent_errorCode_emptyStringReturned() {
        // Arrange
        val mockUri : Uri = mock {
            on { getQueryParameter(ERROR) } doReturn ACCESS_DENIED_ERROR_CODE
            on { getQueryParameter(CODE) } doReturn mockCode
        }
        val mockIntent : Intent = mock { on { data } doReturn mockUri }
        // Act
        val result = sut.extractCodeFromIntent(mockIntent)
        // Assert
        assertThat(result, Is(""))
    }
}
