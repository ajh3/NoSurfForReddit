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

package com.aaronhalbert.nosurfforreddit.data.room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Observable;

@Dao
public interface ClickedPostIdDao {

    //specifying the return type is all that's needed, Room does the rest
    @Query("SELECT * from clicked_post_id_table")
    Observable<List<ClickedPostId>> getAllClickedPostIds();

    // this is a convenience annotation, not necessary to write any SQL for a basic insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertClickedPostId(ClickedPostId clickedPostId);

    // DELETE FROM with no WHERE clause deletes all records in the table
    @SuppressWarnings("unused")
    @Query("DELETE FROM clicked_post_id_table")
    void deleteAllClickedPostIds();
}
