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

import android.view.View;

/* for displaying a Reddit self-type post, as opposed to a link-type
 *
 * also see LinkPostFragment */

public class SelfPostFragment extends PostFragment {

    @Override
    void setupPostViews() {
        fragmentPostBinding
                .postFragmentDividerUnderDetailsForSelfPostsOnly
                .setVisibility(View.VISIBLE);

        if (!"".equals(viewModel.getLastClickedPostDatum().selfTextHtml)) {

            fragmentPostBinding
                    .postFragmentSelftextForSelfPostsOnly
                    .setVisibility(View.VISIBLE);

            fragmentPostBinding
                    .postFragmentDividerUnderSelftextForSelfPostsOnly
                    .setVisibility(View.VISIBLE);
        }
    }

    @Override
    void launchLink(View view, String url) {
        // do nothing; only applies to LinkPostFragment
    }
}
