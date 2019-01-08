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

package com.aaronhalbert.nosurfforreddit.data.local.clickedpostids;

import android.app.Application;

import com.aaronhalbert.nosurfforreddit.data.local.clickedpostids.model.ClickedPostId;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ClickedPostId.class}, version = 1)
public abstract class ClickedPostIdRoomDatabase extends RoomDatabase {

    private static final String CLICKED_POST_ID_DATABASE = "clicked_post_id_database";

    public abstract ClickedPostIdDao clickedPostIdDao();

    private static volatile ClickedPostIdRoomDatabase INSTANCE;

    //prevent having multiple instances of the database opened at the same time
    public static ClickedPostIdRoomDatabase getDatabase(final Application application) {
        if (INSTANCE == null) {
            synchronized (ClickedPostIdRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(application,
                            ClickedPostIdRoomDatabase.class,
                            CLICKED_POST_ID_DATABASE)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
