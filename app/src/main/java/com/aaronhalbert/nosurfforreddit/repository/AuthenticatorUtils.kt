package com.aaronhalbert.nosurfforreddit.repository

import android.content.Intent
import com.aaronhalbert.nosurfforreddit.BuildConfig

const val AUTH_URL_RESPONSE_TYPE = "&response_type="
const val RESPONSE_TYPE = "code"
const val AUTH_URL_STATE = "&state="
const val AUTH_URL_REDIRECT_URI = "&redirect_uri="
const val AUTH_URL_DURATION = "&duration="
const val DURATION = "permanent"
const val AUTH_URL_SCOPE = "&scope="
const val SCOPE = "identity mysubreddits read"

const val ERROR = "error"
const val CODE = "code"

class AuthenticatorUtils(private val randomUUIDWrapper: RandomUUIDWrapper) {

    fun buildAuthUrl(): String {
        return (BuildConfig.AUTH_URL_BASE
                + BuildConfig.CLIENT_ID
                + AUTH_URL_RESPONSE_TYPE
                + RESPONSE_TYPE
                + AUTH_URL_STATE
                + randomUUIDWrapper.randomUUID()
                + AUTH_URL_REDIRECT_URI
                + BuildConfig.REDIRECT_URI
                + AUTH_URL_DURATION
                + DURATION
                + AUTH_URL_SCOPE
                + SCOPE)
    }

    /* Get the one-time use code returned by the Reddit auth server. We later exchange it for a
     * bearer token.
     *
     * Returning an empty string indicates failure; a non-empty value indicates success.
     *
     * Although Reddit sends a descriptive string in case of an error, this function does not
     * report it; all errors are treated as failure and reported as an empty code string
     * regardless of their type. */
    fun extractCodeFromIntent(intent: Intent): String {
        val uri = intent.data ?: return ""
        val error = uri.getQueryParameter(ERROR) ?: ""
        val code = uri.getQueryParameter(CODE) ?: return ""

        when {
            ("" != error) -> return ""
            ("" == code) -> return ""
        }

        return code
    }
}
