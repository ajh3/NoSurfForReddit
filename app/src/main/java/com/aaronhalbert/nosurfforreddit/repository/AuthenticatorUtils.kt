package com.aaronhalbert.nosurfforreddit.repository

import android.content.Intent
import com.aaronhalbert.nosurfforreddit.BuildConfig
import okhttp3.HttpUrl
import java.util.*

const val CLIENT_ID_PARAMETER_NAME = "client_id"
const val AUTH_URL_RESPONSE_TYPE = "response_type"
const val RESPONSE_TYPE = "code"
const val AUTH_URL_STATE = "state"
const val AUTH_URL_REDIRECT_URI = "redirect_uri"
const val AUTH_URL_DURATION = "duration"
const val DURATION = "permanent"
const val AUTH_URL_SCOPE = "scope"
const val SCOPE = "identity mysubreddits read"
const val ERROR = "error"
const val CODE = "code"

class AuthenticatorUtils {

    fun buildAuthUrl(): String {
        return HttpUrl
                .parse(BuildConfig.AUTH_URL_BASE)
                ?.newBuilder()
                ?.addQueryParameter(CLIENT_ID_PARAMETER_NAME, BuildConfig.CLIENT_ID)
                ?.addQueryParameter(AUTH_URL_RESPONSE_TYPE, RESPONSE_TYPE)
                ?.addQueryParameter(AUTH_URL_STATE, UUID.randomUUID().toString())
                ?.addQueryParameter(AUTH_URL_REDIRECT_URI, BuildConfig.REDIRECT_URI)
                ?.addQueryParameter(AUTH_URL_DURATION, DURATION)
                ?.addQueryParameter(AUTH_URL_SCOPE, SCOPE)
                ?.build()
                .toString()
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
