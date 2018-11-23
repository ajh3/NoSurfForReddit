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

    public void setUserOAuthAccessToken(String userOAuthAccessToken) {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_ACCESS_TOKEN, userOAuthAccessToken)
                .apply();
    }

    public void setUserOAuthRefreshToken(String userOAuthRefreshToken) {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_REFRESH_TOKEN, userOAuthRefreshToken)
                .apply();
    }

    public void clearUserOAuthAccessToken() {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_ACCESS_TOKEN, "")
                .apply();
    }

    public void clearUserOAuthRefreshToken() {
        preferences
                .edit()
                .putString(KEY_USER_OAUTH_REFRESH_TOKEN, "")
                .apply();
    }
}
