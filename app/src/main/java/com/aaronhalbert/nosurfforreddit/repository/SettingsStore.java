package com.aaronhalbert.nosurfforreddit.repository;

import com.aaronhalbert.nosurfforreddit.activities.MainActivity;

public interface SettingsStore {
    boolean isNightMode();
    boolean isAmoledNightMode();
    boolean isUseExternalBrowser();
    boolean isDefaultPageSubscribed();
    boolean isNsfwFilter();

    void registerListener(MainActivity mainActivity);
    void unregisterListener(MainActivity mainActivity);
}
