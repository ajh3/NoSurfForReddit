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

package com.aaronhalbert.nosurfforreddit.data.local.settings;

import android.content.SharedPreferences;

public class PreferenceSettingsStore {
    private static final String KEY_NIGHT_MODE = "nightMode";
    private static final String KEY_AMOLED_NIGHT_MODE = "amoledNightMode";
    private static final String KEY_EXTERNAL_BROWSER = "externalBrowser";
    private static final String KEY_SHOW_ALL = "showAll";
    private static final String KEY_DEFAULT_PAGE_IS_ALL = "defaultPageIsAll";
    private static final String KEY_NSFW_FILTER = "nsfwFilter";
    private static final String KEY_SWIPE_TO_REFRESH_DISABLED = "disableSwipeToRefresh";

    private final SharedPreferences preferences;

    public PreferenceSettingsStore(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    // Lint bug thinks method isn't used (?)
    @SuppressWarnings("UnusedReturnValue")
    public boolean isNightMode() {
        return preferences.getBoolean(KEY_NIGHT_MODE, true);
    }

    // Lint bug thinks method isn't used (?)
    @SuppressWarnings("UnusedReturnValue")
    public boolean isAmoledNightMode() {
        return preferences.getBoolean(KEY_AMOLED_NIGHT_MODE, false);
    }

    public boolean isUseExternalBrowser() {
        return preferences.getBoolean(KEY_EXTERNAL_BROWSER, false);
    }

    public boolean showAll() {
        return preferences.getBoolean(KEY_SHOW_ALL, true);
    }

    public boolean isDefaultPageAll() {
        return preferences.getBoolean(KEY_DEFAULT_PAGE_IS_ALL, false);
    }

    public boolean isNsfwFilter() {
        return preferences.getBoolean(KEY_NSFW_FILTER, false);
    }

    public boolean isSwipeToRefreshDisabled() {
        return preferences.getBoolean(KEY_SWIPE_TO_REFRESH_DISABLED, true);
    }

    public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
