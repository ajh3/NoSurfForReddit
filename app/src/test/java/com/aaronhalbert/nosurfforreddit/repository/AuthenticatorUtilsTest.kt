package com.aaronhalbert.nosurfforreddit.repository

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.mockito.Captor
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.Answer

import java.util.ArrayList
import java.util.Collections

import org.junit.Assert.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.mockito.ArgumentCaptor.*

class AuthenticatorUtilsTest {

    private lateinit var sut: AuthenticatorUtils

    @Before
    fun setup() {
        sut = AuthenticatorUtils()


    }

    @Test
    fun buildAuthUrl() {
        // Arrange
        val RESPONSE_TYPE = "code"
        val DURATION = "permanent"
        val SCOPE = "identity mysubreddits read"
        val AUTH_URL_RESPONSE_TYPE = "&response_type="
        val AUTH_URL_STATE = "&state="
        val AUTH_URL_REDIRECT_URI = "&redirect_uri="
        val AUTH_URL_DURATION = "&duration="
        val AUTH_URL_SCOPE = "&scope="
        val ERROR = "error"
        val CODE = "code"
        val ACCESS_DENIED_ERROR_CODE = "access_denied"

        // Act


        // Assert


    }

    @Test
    fun extractCodeFromIntent() {
        // Arrange


        // Act


        // Assert


    }

    // region constants ----------------------------------------------------------------------------

    // endregion constants -------------------------------------------------------------------------


    // region helper fields ------------------------------------------------------------------------

    // endregion helper fields ---------------------------------------------------------------------


    // region helper methods -----------------------------------------------------------------------

    // endregion helper methods --------------------------------------------------------------------


    // region helper classes -----------------------------------------------------------------------

    // endregion helper classes --------------------------------------------------------------------
}
