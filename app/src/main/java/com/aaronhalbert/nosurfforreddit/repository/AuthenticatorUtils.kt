package com.aaronhalbert.nosurfforreddit.repository

import android.content.Intent
import com.aaronhalbert.nosurfforreddit.BuildConfig
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfAccessDeniedLoginException
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfLoginException
import java.util.*

const val RESPONSE_TYPE = "code"
const val DURATION = "permanent"
const val SCOPE = "identity mysubreddits read"
const val AUTH_URL_RESPONSE_TYPE = "&response_type="
const val AUTH_URL_STATE = "&state="
const val AUTH_URL_REDIRECT_URI = "&redirect_uri="
const val AUTH_URL_DURATION = "&duration="
const val AUTH_URL_SCOPE = "&scope="
const val ERROR = "error"
const val CODE = "code"
const val ACCESS_DENIED_ERROR_CODE = "access_denied"

class AuthenticatorUtils {

    fun buildAuthUrl(): String {
        return (BuildConfig.AUTH_URL_BASE
                + BuildConfig.CLIENT_ID
                + AUTH_URL_RESPONSE_TYPE
                + RESPONSE_TYPE
                + AUTH_URL_STATE
                + UUID.randomUUID().toString()
                + AUTH_URL_REDIRECT_URI
                + BuildConfig.REDIRECT_URI
                + AUTH_URL_DURATION
                + DURATION
                + AUTH_URL_SCOPE
                + SCOPE)
    }

    /* get the one-time use code returned by the Reddit auth server. We later exchange it for a
     * bearer token */
    @Throws(NoSurfLoginException::class)
    fun extractCodeFromIntent(intent: Intent): String {

        val uri = intent.data ?: throw NoSurfLoginException()
        val error = uri.getQueryParameter(ERROR) ?: ""
        val code = uri.getQueryParameter(CODE) ?: throw NoSurfLoginException()

        when {
            (ACCESS_DENIED_ERROR_CODE == error) -> throw NoSurfAccessDeniedLoginException()
            ("" == code) -> throw NoSurfLoginException()
        }

        return code
    }
}
