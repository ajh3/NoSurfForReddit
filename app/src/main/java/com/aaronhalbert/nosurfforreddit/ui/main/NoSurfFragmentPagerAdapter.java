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

package com.aaronhalbert.nosurfforreddit.ui.main;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.aaronhalbert.nosurfforreddit.ui.master.AllPostsFragment;
import com.aaronhalbert.nosurfforreddit.ui.master.ContainerFragment;

class NoSurfFragmentPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_ITEMS = 2;
    private static final String R_ALL = "/r/All";
    private static final String YOUR_SUBREDDITS = "Your Subreddits";

    NoSurfFragmentPagerAdapter(Fragment f) {
        super(f);
    }

    @Override
    public int getItemCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return ContainerFragment.newInstance();
        } else {
            return AllPostsFragment.newInstance();
        }
    }

    String getTitle(int position) {
        if (position == 0) {
            return YOUR_SUBREDDITS;
        } else {
            return R_ALL;
        }
    }
}
