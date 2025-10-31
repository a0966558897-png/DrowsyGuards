package com.example.drivesafe.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room Database
 * - version = 2（因為新增了 users 表）
 * - fallbackToDestructiveMigration()：結構變動直接重建 DB，避免 migration 當掉
 */
@Database(
        entities = { FatigueRecord.class, User.class }, // 加入 User
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract FatigueDao fatigueDao();
    public abstract UserDao userDao(); // 新增 UserDao

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "drivesafe.db"
                            )
                            .fallbackToDestructiveMigration() // schema 變動時直接重建
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
