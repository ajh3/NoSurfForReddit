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

package com.aaronhalbert.nosurfforreddit.data.remote.posts

import com.aaronhalbert.nosurfforreddit.data.local.clickedpostids.model.ClickedPostId

//TODO: extract other helper methods out of PostsRepo into here and write unit tests as I go
class PostsRepoUtils {

    fun convertListOfClickedPostIdsToListOfStrings(input: List<ClickedPostId>): List<String> {

        return input.map { it.clickedPostId }
    }
}
