package com.aaronhalbert.nosurfforreddit.room;

import android.app.Application;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
                            .addCallback(clickedPostIdRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // called once, when the DB is first created
    private static RoomDatabase.Callback clickedPostIdRoomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate (@NonNull SupportSQLiteDatabase db){
            super.onCreate(db);
            //new InitDbAsyncTask(INSTANCE).execute();
        }
    };

    private static class InitDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private ClickedPostIdDao dao;

        InitDbAsyncTask(ClickedPostIdRoomDatabase db) {
            dao = db.clickedPostIdDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // empty placeholder for now
            // make calls here on the dao reference variable

            return null;
        }
    }
}