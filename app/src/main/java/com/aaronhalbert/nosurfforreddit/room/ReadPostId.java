package com.aaronhalbert.nosurfforreddit.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "read_post_id_table")
public class ReadPostId {

    @PrimaryKey //this field is its own primary key
    @NonNull
    private String readPostId;

    public ReadPostId(@NonNull String readPostId) {
        this.readPostId = readPostId;
    }

    public String getReadPostId() {
        return this.readPostId;
    }
}
