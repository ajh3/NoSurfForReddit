package com.aaronhalbert.nosurfforreddit.repository;

import android.content.SharedPreferences;

public class PreferenceTokenStore implements TokenStore {
    private static final String KEY_USER_OAUTH_ACCESS_TOKEN = "userAccessToken";
    private static final String KEY_USER_OAUTH_REFRESH_TOKEN = "userAccessRefreshToken";

    private final SharedPreferences preferences;

    public PreferenceTokenStore(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public String getUserOAuthAccessToken() {
        return preferences.getString(KEY_USER_OAUTH_ACCESS_TOKEN, "");
    }

    @Override
    public String getUserOAuthRefreshToken() {
        return preferences.getString(KEY_USER_OAUTH_REFRESH_TOKEN, "");
    }

    @Override
    public void setUserOAuthAccessTokenAsync(String userOAuthAccessToken) {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_ACCESS_TOKEN, userOAuthAccessToken)
                .apply();
    }

    @Override
    public void setUserOAuthRefreshTokenAsync(String userOAuthRefreshToken) {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_REFRESH_TOKEN, userOAuthRefreshToken)
                .apply();
    }

    @Override
    public void clearUserOAuthAccessTokenAsync() {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_ACCESS_TOKEN, "")
                .apply();
    }

    @Override
    public void clearUserOAuthRefreshTokenAsync() {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_REFRESH_TOKEN, "")
                .apply();
    }
}
