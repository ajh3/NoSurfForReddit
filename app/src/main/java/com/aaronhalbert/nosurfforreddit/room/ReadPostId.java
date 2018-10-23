package com.aaronhalbert.nosurfforreddit.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A basic class representing an entity that is a row in a one-column database table.
 *
 * @ Entity - You must annotate the class as an entity and supply a table name if not class name.
 * @ PrimaryKey - You must identify the primary key.
 * @ ColumnInfo - You must supply the column name if it is different from the variable name.
 *
 * See the documentation for the full rich set of annotations.
 * https://developer.android.com/topic/libraries/architecture/room.html
 */

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



