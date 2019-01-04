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

package com.aaronhalbert.nosurfforreddit.utils.webview;

import android.webkit.WebView;

import androidx.fragment.app.FragmentActivity;

// also see NoSurfWebViewClient; Dagger injects correct version based on API level

public class NoSurfWebViewClientApiLevelBelow24 extends NoSurfWebViewClient {
    public NoSurfWebViewClientApiLevelBelow24(FragmentActivity hostFragmentActivity) {
        super(hostFragmentActivity);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        return processUrl(url);
    }
}
