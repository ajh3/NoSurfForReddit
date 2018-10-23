package com.aaronhalbert.nosurfforreddit.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReadPostIdDao {

    @Query("SELECT * from read_post_id_table")
    LiveData<List<ReadPostId>> getAllReadPostIds(); //specifying the return type is all that's needed, Room does the rest

    // We do not need a conflict strategy, because the post id is our primary key, and you cannot
    // add two items with the same primary key to the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE) //convenience annotation, not necessary to write any SQL
    void insertReadPostId(ReadPostId readPostId);

    @Query("DELETE FROM read_post_id_table")
    void deleteAllReadPostIds();
}
