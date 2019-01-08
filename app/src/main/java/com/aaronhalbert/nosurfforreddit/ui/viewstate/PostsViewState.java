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

package com.aaronhalbert.nosurfforreddit.ui.viewstate;

import java.util.ArrayList;

/* data structure to hold cleaned/transformed post data from the Reddit API */

public class PostsViewState {
    private static final int INITIAL_CAPACITY = 25;
    public final ArrayList<PostDatum> postData;
    public final boolean[] hasBeenClicked;

    public PostsViewState() {
        postData = new ArrayList<>(INITIAL_CAPACITY);
        hasBeenClicked = new boolean[INITIAL_CAPACITY];

        // make sure initial *size* (not just capacity) of postData == INITIAL_CAPACITY
        // so we can start set()ing its elements right away from the ViewModel
        for (int i = 0; i < INITIAL_CAPACITY; i++) {
            postData.add(new PostDatum());
        }
    }

    public static class PostDatum {
        public boolean isSelf;
        public boolean isNsfw;
        public int score;
        public int numComments;
        public String id = "";
        public String title = "";
        public String author = "";
        public String subreddit = "";
        public String url = "";
        public String thumbnailUrl = "";
        public String imageUrl = "";
        public String selfTextHtml = "";
        public String permalink = "";
    }
}
