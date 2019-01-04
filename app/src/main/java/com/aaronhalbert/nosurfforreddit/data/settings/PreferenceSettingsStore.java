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

package com.aaronhalbert.nosurfforreddit.data.settings;

import android.content.SharedPreferences;

import com.aaronhalbert.nosurfforreddit.ui.main.MainActivity;

public class PreferenceSettingsStore implements SettingsStore {
    private static final String KEY_NIGHT_MODE = "nightMode";
    private static final String KEY_AMOLED_NIGHT_MODE = "amoledNightMode";
    private static final String KEY_EXTERNAL_BROWSER = "externalBrowser";
    private static final String KEY_DEFAULT_PAGE_IS_SUBSCRIBED = "defaultPageIsSubscribed";
    private static final String KEY_NSFW_FILTER = "nsfwFilter";

    private final SharedPreferences preferences;

    public PreferenceSettingsStore(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean isNightMode() {
        return preferences.getBoolean(KEY_NIGHT_MODE, true);
    }

    @Override
    public boolean isAmoledNightMode() {
        return preferences.getBoolean(KEY_AMOLED_NIGHT_MODE, false);
    }

    @Override
    public boolean isUseExternalBrowser() {
        return preferences.getBoolean(KEY_EXTERNAL_BROWSER, false);
    }

    @Override
    public boolean isDefaultPageSubscribed() {
        return preferences.getBoolean(KEY_DEFAULT_PAGE_IS_SUBSCRIBED, false);
    }

    @Override
    public boolean isNsfwFilter() {
        return preferences.getBoolean(KEY_NSFW_FILTER, false);
    }

    @Override
    public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
