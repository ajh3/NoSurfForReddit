/*
 * Copyright (c) 2018-present, Aaron J. Halbert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.aaronhalbert.nosurfforreddit.data.local.auth;

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
