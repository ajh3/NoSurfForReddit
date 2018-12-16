package com.aaronhalbert.nosurfforreddit.repository;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.aaronhalbert.nosurfforreddit.BuildConfig;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfAccessDeniedLoginException;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfLoginException;
import com.aaronhalbert.nosurfforreddit.repository.redditschema.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.repository.redditschema.UserOAuthToken;

import java.util.UUID;

import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import retrofit2.Retrofit;

/* see https://github.com/reddit-archive/reddit/wiki/OAuth2 for Reddit login API documentation */

public class NoSurfAuthenticator {
    private static final String RESPONSE_TYPE = "code";
    private static final String DURATION = "permanent";
    private static final String SCOPE = "identity mysubreddits read";
    private static final String AUTH_URL_RESPONSE_TYPE = "&response_type=";
    private static final String AUTH_URL_STATE = "&state=";
    private static final String AUTH_URL_REDIRECT_URI = "&redirect_uri=";
    private static final String AUTH_URL_DURATION = "&duration=";
    private static final String AUTH_URL_SCOPE = "&scope=";
    private static final String USER_GRANT_TYPE = "authorization_code";
    private static final String USER_REFRESH_GRANT_TYPE = "refresh_token";
    private static final String DEVICE_ID = "DO_NOT_TRACK_THIS_DEVICE";
    private static final String AUTH_HEADER = okhttp3.Credentials.basic(BuildConfig.CLIENT_ID, "");

    // caches to let us keep working during asynchronous writes to SharedPrefs
    private String userOAuthAccessTokenCache = "";
    private String userOAuthRefreshTokenCache = "";
    private String appOnlyOAuthTokenCache = "";
    private boolean isUserLoggedInCache;

    // user login status
    private final BehaviorSubject<Boolean> isUserLoggedIn = BehaviorSubject.createDefault(false);

    // other
    private final Application application;
    private final RetrofitAuthenticationInterface ri;
    private final TokenStore tokenStore;

    public NoSurfAuthenticator(Application application,
                               Retrofit retrofit,
                               TokenStore tokenStore) {
        this.application = application;
        ri = retrofit.create(RetrofitAuthenticationInterface.class);
        this.tokenStore = tokenStore;
    }

    // region network auth calls -------------------------------------------------------------------

    /* Logged-out (aka anonymous, aka app-only users require an anonymous token to interact with
     * the Reddit API and view public posts and comments from r/all. This token is provided by
     * fetchAppOnlyOAuthTokenASync() and does not require any user credentials.
     *
     *  Also called to refresh this anonymous token when it expires */
    Single<AppOnlyOAuthToken> fetchAppOnlyOAuthTokenASync() {
        return ri.fetchAppOnlyOAuthTokenASync(
                BuildConfig.OAUTH_BASE_URL,
                BuildConfig.APP_ONLY_GRANT_TYPE,
                DEVICE_ID,
                AUTH_HEADER)
                .doOnSuccess(appOnlyOAuthToken -> {
                    appOnlyOAuthTokenCache = appOnlyOAuthToken.getAccessToken();
                    // don't bother saving this ephemeral token into sharedprefs
                });
    }

    /* Logged-in users can view posts from their subscribed subreddits in addition to r/all,
     * but this requires a user OAuth token, which is provided by fetchUserOAuthTokenASync().
     *
     * Note that after the user is logged in, their user token is now also used for viewing
     * r/all, instead of the previously-fetched anonymous token from fetchAppOnlyOAuthTokenASync. */
    Single<UserOAuthToken> fetchUserOAuthTokenASync(String code) {
        return ri.fetchUserOAuthTokenASync(
                BuildConfig.OAUTH_BASE_URL,
                USER_GRANT_TYPE,
                code,
                BuildConfig.REDIRECT_URI,
                AUTH_HEADER)
                .doOnSuccess(this::loginSuccess);
    }

    /* unlike the app-only, logged-out auth token, user auth tokens require a special refresh
     * call */
    Single<UserOAuthToken> refreshExpiredUserOAuthTokenASync() {
        return ri.refreshExpiredUserOAuthTokenASync(
                BuildConfig.OAUTH_BASE_URL,
                USER_REFRESH_GRANT_TYPE,
                userOAuthRefreshTokenCache,
                AUTH_HEADER)
                .doOnSuccess(this::loginSuccess);
    }

    // endregion network auth calls ----------------------------------------------------------------

    // region init/de-init methods -----------------------------------------------------------------

    /* login credentials are stored in SharedPreferences to survive not only config changes,
     *  but process termination and reboots, so user doesn't have to re-login every time the app
     *  exits.
     *
     *  This method should run on app initialization, to see if the user's credentials have been
     *  previously saved. */
    void checkIfLoginCredentialsAlreadyExist() {
        userOAuthAccessTokenCache = tokenStore.getUserOAuthAccessToken();
        userOAuthRefreshTokenCache = tokenStore.getUserOAuthRefreshToken();

        if (!"".equals(userOAuthAccessTokenCache) && !"".equals(userOAuthRefreshTokenCache)) {
            setUserLoggedIn();
        } else {
            // need to explicitly call this here to help ContainerFragment set itself up
            setUserLoggedOut();
        }
    }

    void setUserLoggedIn() {
        isUserLoggedInCache = true;
        isUserLoggedIn.onNext(true);
    }

    void setUserLoggedOut() {
        clearCookies();

        isUserLoggedInCache = false;
        isUserLoggedIn.onNext(false);

        userOAuthAccessTokenCache = "";
        userOAuthRefreshTokenCache = "";

        tokenStore.clearUserOAuthAccessTokenAsync();
        tokenStore.clearUserOAuthRefreshTokenAsync();
    }

    private void loginSuccess(UserOAuthToken userOAuthToken) {
        userOAuthAccessTokenCache = userOAuthToken.getAccessToken();
        userOAuthRefreshTokenCache = userOAuthToken.getRefreshToken();

        tokenStore.setUserOAuthAccessTokenAsync(userOAuthToken.getAccessToken());
        tokenStore.setUserOAuthRefreshTokenAsync(userOAuthToken.getRefreshToken());

        setUserLoggedIn();
    }

    // endregion init/de-init methods --------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    // clears cookies in the app's WebView
    private void clearCookies() {
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

    // endregion helper methods --------------------------------------------------------------------

    // region getters and setters ------------------------------------------------------------------

    String getUserOAuthAccessTokenCache() {
        return userOAuthAccessTokenCache;
    }

    String getAppOnlyOAuthTokenCache() {
        return appOnlyOAuthTokenCache;
    }

    boolean isUserLoggedInCache() {
        return isUserLoggedInCache;
    }

    BehaviorSubject<Boolean> getIsUserLoggedIn() {
        return isUserLoggedIn;
    }

    // endregion getters and setters ---------------------------------------------------------------

    // region static methods -----------------------------------------------------------------------

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

    // endregion static methods --------------------------------------------------------------------
}
