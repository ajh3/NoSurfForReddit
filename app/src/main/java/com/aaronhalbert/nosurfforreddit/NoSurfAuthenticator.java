package com.aaronhalbert.nosurfforreddit;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfAccessDeniedLoginException;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfLoginException;

import java.util.UUID;

/* see https://github.com/reddit-archive/reddit/wiki/OAuth2 */

public class NoSurfAuthenticator {
    private static final String ERROR = "error";
    private static final String CODE = "code";
    private static final String ACCESS_DENIED_ERROR_CODE = "access_denied";
    private static final String RESPONSE_TYPE = "code";
    private static final String DURATION = "permanent";
    private static final String SCOPE = "identity mysubreddits read";
    private static final String AUTH_URL_RESPONSE_TYPE = "&response_type=";
    private static final String AUTH_URL_STATE = "&state=";
    private static final String AUTH_URL_REDIRECT_URI = "&redirect_uri=";
    private static final String AUTH_URL_DURATION = "&duration=";
    private static final String AUTH_URL_SCOPE = "&scope=";

    private final Application application;

    public NoSurfAuthenticator(Application application) {
        this.application = application;
    }

    public String buildAuthUrl() {
        return BuildConfig.AUTH_URL_BASE
                + BuildConfig.CLIENT_ID
                + AUTH_URL_RESPONSE_TYPE
                + RESPONSE_TYPE
                + AUTH_URL_STATE
                + generateRandomAlphaNumericString()
                + AUTH_URL_REDIRECT_URI
                + BuildConfig.REDIRECT_URI
                + AUTH_URL_DURATION
                + DURATION
                + AUTH_URL_SCOPE
                + SCOPE;
    }

    // clears cookies in the app's WebView
    public void clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(application);
            cookieSyncManager.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
    }

    /* get the one-time use code returned by the Reddit auth server. We later exchange it for a
     * bearer token */
    public String extractCodeFromIntent(Intent i) throws NoSurfLoginException {
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

    private String generateRandomAlphaNumericString() {
        return UUID.randomUUID().toString();
    }
}
