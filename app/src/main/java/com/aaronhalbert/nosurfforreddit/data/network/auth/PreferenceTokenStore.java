package com.aaronhalbert.nosurfforreddit.data.network.auth;

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
        editPrefs(KEY_USER_OAUTH_ACCESS_TOKEN, userOAuthAccessToken);
    }

    @Override
    public void setUserOAuthRefreshTokenAsync(String userOAuthRefreshToken) {
        editPrefs(KEY_USER_OAUTH_REFRESH_TOKEN, userOAuthRefreshToken);
    }

    @Override
    public void clearUserOAuthAccessTokenAsync() {
        editPrefs(KEY_USER_OAUTH_ACCESS_TOKEN, "");
    }

    @Override
    public void clearUserOAuthRefreshTokenAsync() {
        editPrefs(KEY_USER_OAUTH_REFRESH_TOKEN, "");
    }

    private void editPrefs(String key, String value) {
        preferences
                .edit()
                .putString(key, value)
                .apply();
    }
}
