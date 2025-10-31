package com.example.drivesafe.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    // ---------- 新增 ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    // ---------- 更新 ----------
    @Update
    int update(User user);

    // ---------- 刪除 ----------
    @Delete
    int delete(User user);

    @Query("DELETE FROM users")
    void deleteAll();

    // ---------- 查詢 ----------
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    List<User> getAll();

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getById(long id);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getByEmail(String email);

    @Query("SELECT COUNT(*) FROM users")
    int count();

    // ---------- 關聯查詢 ----------
    /** 一次查出使用者與其所有疲勞紀錄 */
    @Transaction
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    List<UserWithRecords> getUsersWithRecords();
}
