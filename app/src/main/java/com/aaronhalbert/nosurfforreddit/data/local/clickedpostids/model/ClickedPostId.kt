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

package com.aaronhalbert.nosurfforreddit.data.local.clickedpostids.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clicked_post_id_table")
data class ClickedPostId(
    //Note to self: this field is its own primary key
    @field:PrimaryKey val clickedPostId: String
)
