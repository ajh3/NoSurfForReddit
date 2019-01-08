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

package com.aaronhalbert.nosurfforreddit.ui.viewstate

import android.text.Spanned

/* data structure to hold cleaned/transformed comment data from the Reddit API */

data class CommentsViewState(val numComments: Int, val id: String) {
    val commentBodies: Array<Spanned?> = arrayOfNulls(numComments)
    val commentDetails: Array<String?> = arrayOfNulls(numComments)
}
