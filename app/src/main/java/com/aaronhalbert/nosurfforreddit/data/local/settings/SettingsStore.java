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
