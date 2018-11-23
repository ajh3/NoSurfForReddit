package com.aaronhalbert.nosurfforreddit.repository;

import android.content.SharedPreferences;

import com.aaronhalbert.nosurfforreddit.activities.MainActivity;

public class PreferenceSettingsStore implements SettingsStore {
    private static final String KEY_NIGHT_MODE = "nightMode";
    private static final String KEY_AMOLED_NIGHT_MODE = "amoledNightMode";
    private static final String KEY_EXTERNAL_BROWSER = "externalBrowser";

    private final SharedPreferences preferences;

    public PreferenceSettingsStore(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean isNightMode() {
        return preferences.getBoolean(KEY_NIGHT_MODE, true);
    }

    public boolean isAmoledNightMode() {
        return preferences.getBoolean(KEY_AMOLED_NIGHT_MODE, false);
    }

    public boolean isUseExternalBrowser() {
        return preferences.getBoolean(KEY_EXTERNAL_BROWSER, false);
    }

    public void registerListener(MainActivity mainActivity) {
        preferences.registerOnSharedPreferenceChangeListener(mainActivity);
    }

    public void unregisterListener(MainActivity mainActivity) {
        preferences.unregisterOnSharedPreferenceChangeListener(mainActivity);
    }
}
