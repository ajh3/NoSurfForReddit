package com.aaronhalbert.nosurfforreddit.data.repository;

import com.aaronhalbert.nosurfforreddit.ui.main.MainActivity;

public interface SettingsStore {
    boolean isNightMode();
    boolean isAmoledNightMode();
    boolean isUseExternalBrowser();
    boolean isDefaultPageSubscribed();
    boolean isNsfwFilter();

    void registerListener(MainActivity mainActivity);
    void unregisterListener(MainActivity mainActivity);
}
