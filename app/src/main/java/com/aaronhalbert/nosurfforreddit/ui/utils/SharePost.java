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

package com.aaronhalbert.nosurfforreddit.ui.utils;

import android.content.Context;
import android.content.Intent;

import com.aaronhalbert.nosurfforreddit.R;

import static com.aaronhalbert.nosurfforreddit.BuildConfig.REDDIT_URL_BASE;

/* I prefer this custom solution to ShareActionProvider. Don't like the "frequent" shortcut
 * it puts in the action bar, or the border it draws between the icons. The standard chooser
 * is preferable IMO. */
public class SharePost {
    private static final String SHARE_POST = "Share post...";

    private final Context context;
    private Intent intent;

    public SharePost(Context context) {
        this.context = context;
    }

    public void createShareIntent(String lastClickedPostPermalink) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, SHARE_POST);
        i.putExtra(Intent.EXTRA_TEXT, REDDIT_URL_BASE + lastClickedPostPermalink);

        this.intent = i;
    }

    public void launchShareIntent() {
        context.startActivity(Intent.createChooser(intent, context.getResources().getText(R.string.share)));
    }
}
