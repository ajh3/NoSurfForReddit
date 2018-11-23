package com.aaronhalbert.nosurfforreddit.repository;

import android.content.SharedPreferences;

public class PreferenceTokenStore implements TokenStore {
    private static final String KEY_USER_OAUTH_ACCESS_TOKEN = "userAccessToken";
    private static final String KEY_USER_OAUTH_REFRESH_TOKEN = "userAccessRefreshToken";

    private final SharedPreferences preferences;

    public PreferenceTokenStore(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public String getUserOAuthAccessToken() {
        return preferences.getString(KEY_USER_OAUTH_ACCESS_TOKEN, "");
    }

    public String getUserOAuthRefreshToken() {
        return preferences.getString(KEY_USER_OAUTH_REFRESH_TOKEN, "");
    }

    public void setUserOAuthAccessTokenAsync(String userOAuthAccessToken) {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_ACCESS_TOKEN, userOAuthAccessToken)
                .apply();
    }

    public void setUserOAuthRefreshTokenAsync(String userOAuthRefreshToken) {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_REFRESH_TOKEN, userOAuthRefreshToken)
                .apply();
    }

    public void clearUserOAuthAccessTokenAsync() {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_ACCESS_TOKEN, "")
                .apply();
    }

    public void clearUserOAuthRefreshTokenAsync() {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_REFRESH_TOKEN, "")
                .apply();
    }
}
