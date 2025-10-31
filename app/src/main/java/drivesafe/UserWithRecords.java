package com.example.drivesafe.db;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

public class UserWithRecords {
    @Embedded
    public User user;

    @Relation(
            parentColumn = "id",
            entityColumn = "userId"
    )
    public List<FatigueRecord> records;
}
