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

import android.app.Application;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@SuppressWarnings("ALL")
@Database(entities = {ClickedPostId.class}, version = 1)
public abstract class ClickedPostIdRoomDatabase extends RoomDatabase {

    public abstract ClickedPostIdDao clickedPostIdDao();
    private static volatile ClickedPostIdRoomDatabase INSTANCE;

    //prevent having multiple instances of the database opened at the same time
    public static ClickedPostIdRoomDatabase getDatabase(final Application application) {
        if (INSTANCE == null) {
            synchronized (ClickedPostIdRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(application,
                            ClickedPostIdRoomDatabase.class,
                            "clicked_post_id_database")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration()
                            //.addCallback(clickedPostIdRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // called once when the DB is first created, if addCallback is called above
    private static final RoomDatabase.Callback clickedPostIdRoomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate (SupportSQLiteDatabase db){
            super.onCreate(db);
            //do work on background thread here
        }
    };
}