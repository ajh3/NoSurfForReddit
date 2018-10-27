package com.aaronhalbert.nosurfforreddit.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "clicked_post_id_table")
public class ClickedPostId {

    @PrimaryKey //this field is its own primary key
    @NonNull
    private String clickedPostId;

    public ClickedPostId(@NonNull String clickedPostId) {
        this.clickedPostId = clickedPostId;
    }

    public String getClickedPostId() {
        return this.clickedPostId;
    }
}
