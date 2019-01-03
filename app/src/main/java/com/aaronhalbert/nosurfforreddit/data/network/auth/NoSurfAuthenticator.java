package com.aaronhalbert.nosurfforreddit.data.network.auth;

import android.app.Application;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.aaronhalbert.nosurfforreddit.BuildConfig;
import com.aaronhalbert.nosurfforreddit.data.model.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.data.model.UserOAuthToken;

import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/* see https://github.com/reddit-archive/reddit/wiki/OAuth2 for Reddit login API documentation */

public class NoSurfAuthenticator {
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
    private final BehaviorSubject<Boolean> isUserLoggedIn = BehaviorSubject.create();

    // other
    private final Application application;
    private final RetrofitAuthenticationInterface ri;
    private final TokenStore tokenStore;

    public NoSurfAuthenticator(Application application,
                               RetrofitAuthenticationInterface ri,
                               TokenStore tokenStore) {
        this.application = application;
        this.ri = ri;
        this.tokenStore = tokenStore;
    }

    // region network auth calls -------------------------------------------------------------------

    /* Logged-out (aka anonymous, aka app-only users require an anonymous token to interact with
     * the Reddit API and view public posts and comments from r/all. This token is provided by
     * fetchAppOnlyOAuthTokenASync() and does not require any user credentials.
     *
     *  Also called to refresh this anonymous token when it expires */
    public Single<AppOnlyOAuthToken> fetchAppOnlyOAuthTokenASync() {
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
    public Single<UserOAuthToken> fetchUserOAuthTokenASync(String code) {
        return ri.fetchUserOAuthTokenASync(
                BuildConfig.OAUTH_BASE_URL,
                USER_GRANT_TYPE,
                code,
                BuildConfig.REDIRECT_URI,
                AUTH_HEADER)
                .doOnSuccess(token -> loginSuccess(token, false));
    }

    /* unlike the app-only, logged-out auth token, user auth tokens require a special refresh
     * call */
    public Single<UserOAuthToken> refreshExpiredUserOAuthTokenASync() {
        return ri.refreshExpiredUserOAuthTokenASync(
                BuildConfig.OAUTH_BASE_URL,
                USER_REFRESH_GRANT_TYPE,
                userOAuthRefreshTokenCache,
                AUTH_HEADER)
                .doOnSuccess(token -> loginSuccess(token, true));
    }

    // endregion network auth calls ----------------------------------------------------------------

    // region init/de-init methods -----------------------------------------------------------------

    /* login credentials are stored in SharedPreferences to survive not only config changes,
     *  but process termination and reboots, so user doesn't have to re-login every time the app
     *  exits.
     *
     *  This method should run on app initialization, to see if the user's credentials have been
     *  previously saved. */
    public void checkIfLoginCredentialsAlreadyExist() {
        userOAuthAccessTokenCache = tokenStore.getUserOAuthAccessToken();
        userOAuthRefreshTokenCache = tokenStore.getUserOAuthRefreshToken();

        if (!"".equals(userOAuthAccessTokenCache) && !"".equals(userOAuthRefreshTokenCache)) {
            setUserLoggedIn();
        } else {
            // need to explicitly call this here to help ContainerFragment set itself up
            setUserLoggedOut();
        }
    }

    private void setUserLoggedIn() {
        isUserLoggedInCache = true;
        isUserLoggedIn.onNext(true);
    }

    public void setUserLoggedOut() {
        clearCookies();

        isUserLoggedInCache = false;
        isUserLoggedIn.onNext(false);

        userOAuthAccessTokenCache = "";
        userOAuthRefreshTokenCache = "";

        tokenStore.clearUserOAuthAccessTokenAsync();
        tokenStore.clearUserOAuthRefreshTokenAsync();
    }

    private void loginSuccess(UserOAuthToken userOAuthToken, boolean isRefresh) {
        userOAuthAccessTokenCache = userOAuthToken.getAccessToken();
        tokenStore.setUserOAuthAccessTokenAsync(userOAuthAccessTokenCache);

        /* we only get a refresh token from the initial fetchUserOAuthTokenASync(), call, and
         * the same refresh token continues working as long as the user stays logged in. Refresh
         * calls to refreshExpiredUserOAuthTokenASync() return a new user auth token, but NOT a new
         * refresh token, so don't try to save one. */
        if (!isRefresh) {
            userOAuthRefreshTokenCache = userOAuthToken.getRefreshToken();
            tokenStore.setUserOAuthRefreshTokenAsync(userOAuthRefreshTokenCache);
        }

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

    public String getUserOAuthAccessTokenCache() {
        return userOAuthAccessTokenCache;
    }

    public String getAppOnlyOAuthTokenCache() {
        return appOnlyOAuthTokenCache;
    }

    public boolean isUserLoggedInCache() {
        return isUserLoggedInCache;
    }

    public BehaviorSubject<Boolean> getIsUserLoggedIn() {
        return isUserLoggedIn;
    }

    // endregion getters and setters ---------------------------------------------------------------
}
