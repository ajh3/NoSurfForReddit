package com.aaronhalbert.nosurfforreddit.data.settings;

import android.content.SharedPreferences;

@SuppressWarnings("UnusedReturnValue")
public interface SettingsStore {
    boolean isNightMode();
    boolean isAmoledNightMode();
    boolean isUseExternalBrowser();
    boolean isDefaultPageSubscribed();
    boolean isNsfwFilter();

    void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener);
    void unregisterListener(SharedPreferences.OnSharedPreferenceChangeListener listener);
}
