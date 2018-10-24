package com.aaronhalbert.nosurfforreddit.room;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.util.Log;

@Database(entities = {ReadPostId.class}, version = 1)
public abstract class ReadPostIdRoomDatabase extends RoomDatabase {

    public abstract ReadPostIdDao readPostIdDao();
    private static volatile ReadPostIdRoomDatabase INSTANCE;

    //prevent having multiple instances of the database opened at the same time
    public static ReadPostIdRoomDatabase getDatabase(final Application application) {
        if (INSTANCE == null) {
            synchronized (ReadPostIdRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(application,
                            ReadPostIdRoomDatabase.class,
                            "read_post_id_database")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration()
                            .addCallback(readPostIdRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // called once, when the DB is first created
    private static RoomDatabase.Callback readPostIdRoomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate (@NonNull SupportSQLiteDatabase db){
            super.onCreate(db);
            new InitDbAsyncTask(INSTANCE).execute();
        }
    };

    private static class InitDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private ReadPostIdDao dao;

        InitDbAsyncTask(ReadPostIdRoomDatabase db) {
            dao = db.readPostIdDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // empty placeholder for now
            // make calls here on the dao reference variable

            return null;
        }
    }
}