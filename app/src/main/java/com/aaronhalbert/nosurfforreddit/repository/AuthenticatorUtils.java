package com.aaronhalbert.nosurfforreddit.repository;

import android.content.Intent;
import android.net.Uri;

import com.aaronhalbert.nosurfforreddit.BuildConfig;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfAccessDeniedLoginException;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfLoginException;

import java.util.UUID;

public class AuthenticatorUtils {
    private static final String RESPONSE_TYPE = "code";
    private static final String DURATION = "permanent";
    private static final String SCOPE = "identity mysubreddits read";
    private static final String AUTH_URL_RESPONSE_TYPE = "&response_type=";
    private static final String AUTH_URL_STATE = "&state=";
    private static final String AUTH_URL_REDIRECT_URI = "&redirect_uri=";
    private static final String AUTH_URL_DURATION = "&duration=";
    private static final String AUTH_URL_SCOPE = "&scope=";

    public static String buildAuthUrl() {
        return BuildConfig.AUTH_URL_BASE
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
                + SCOPE;
    }

    /* get the one-time use code returned by the Reddit auth server. We later exchange it for a
     * bearer token */
    public static String extractCodeFromIntent(Intent i) throws NoSurfLoginException {
        final String ERROR = "error";
        final String CODE = "code";
        final String ACCESS_DENIED_ERROR_CODE = "access_denied";

        Uri uri;
        String error;
        String code;

        if (i.getData() != null) {
            uri = i.getData();
        } else {
            throw new NoSurfLoginException();
        }

        if (uri != null) {
            error = uri.getQueryParameter(ERROR);
            code = uri.getQueryParameter(CODE);
        } else {
            throw new NoSurfLoginException();
        }

        if (ACCESS_DENIED_ERROR_CODE.equals(error)) {
            throw new NoSurfAccessDeniedLoginException();
        } else if ("".equals(error)) {
            throw new NoSurfLoginException();
        }

        if (code != null && !"".equals(code)) {
            return code;
        } else {
            throw new NoSurfLoginException();
        }
    }
}