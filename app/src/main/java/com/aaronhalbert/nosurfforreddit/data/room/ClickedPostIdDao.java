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
