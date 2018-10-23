package com.aaronhalbert.nosurfforreddit.room;

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

    public static ReadPostIdRoomDatabase getDatabase(final Context context) { //prevent having multiple instances of the database opened at the same time
        if (INSTANCE == null) {
            synchronized (ReadPostIdRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ReadPostIdRoomDatabase.class, "read_post_id_database")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration()
                            .addCallback(readPostIdRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback readPostIdRoomDatabaseCallback = new RoomDatabase.Callback(){

        @Override
        public void onCreate (@NonNull SupportSQLiteDatabase db){
            super.onCreate(db);
            Log.e(getClass().toString(), "database created");
            new PopulateDbAsyncTask(INSTANCE).execute();
        }
    };




    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private ReadPostIdDao dao;

        PopulateDbAsyncTask(ReadPostIdRoomDatabase db) {
            dao = db.readPostIdDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            dao.deleteAllReadPostIds();

            Log.e(getClass().toString(), "after deleting all posts in db");

            ReadPostId readPostId = new ReadPostId("Hello");
            dao.insertReadPostId(readPostId);
            readPostId = new ReadPostId("World");
            dao.insertReadPostId(readPostId);
            return null;
        }
    }
}