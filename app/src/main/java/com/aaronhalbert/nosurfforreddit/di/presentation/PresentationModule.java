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

package com.aaronhalbert.nosurfforreddit.di.presentation;

import android.os.Build;

import com.aaronhalbert.nosurfforreddit.ui.utils.webview.NoSurfWebViewClient;
import com.aaronhalbert.nosurfforreddit.ui.utils.webview.NoSurfWebViewClientApiLevelBelow24;

import androidx.fragment.app.FragmentActivity;
import dagger.Module;
import dagger.Provides;

@Module
public class PresentationModule {
    private final FragmentActivity fragmentActivity;

    public PresentationModule(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
    }

    @Provides
    NoSurfWebViewClient provideNoSurfWebViewClient() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return new NoSurfWebViewClient(fragmentActivity);
        } else {
            return new NoSurfWebViewClientApiLevelBelow24(fragmentActivity);
        }
    }
}
