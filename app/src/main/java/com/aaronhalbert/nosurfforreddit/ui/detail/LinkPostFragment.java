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

package com.aaronhalbert.nosurfforreddit.ui.detail;

import android.net.Uri;
import android.view.View;

import com.aaronhalbert.nosurfforreddit.utils.ExternalBrowser;

import androidx.navigation.Navigation;

import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.GotoUrlGlobalAction;
import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.gotoUrlGlobalAction;

/* for displaying a Reddit link-type post, as opposed to a self-type
 *
 * also see SelfPostFragment */

public class LinkPostFragment extends PostFragment {

    @Override
    void setupPostViews() {
        fragmentPostBinding
                .postFragmentImageForLinkPostsOnly
                .setVisibility(View.VISIBLE);
    }

    @Override
    void launchLink(View view, String url) {
        if (externalBrowser) {
            ExternalBrowser e = new ExternalBrowser(getContext());
            e.launchExternalBrowser(Uri.parse(url));
        } else {
            GotoUrlGlobalAction action
                    = gotoUrlGlobalAction(url);

            Navigation.findNavController(view).navigate(action);
        }
    }
}
