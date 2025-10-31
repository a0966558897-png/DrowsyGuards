package com.example.drivesafe.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// ✅ 讓 Java 也能回傳 Kotlin Flow
import kotlinx.coroutines.flow.Flow;

/** DAO：加入 LiveData 觀察版（給 AdminActivity 用），並保留同步/統計方法 */
@Dao
public interface FatigueDao {

    // ---------- INSERT ----------
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(FatigueRecord r);

    /** 批次新增（伺服器寫回本機常用）；REPLACE 避免重複衝突 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<FatigueRecord> list);

    // ---------- UPDATE / DELETE ----------
    @Update
    int update(FatigueRecord r);

    @Delete
    int delete(FatigueRecord r);

    /** 全刪（重置/除錯） */
    @Query("DELETE FROM fatigue_records")
    void deleteAll();

    /** 同 deleteAll；AdminActivity 會呼叫這個名字 */
    @Query("DELETE FROM fatigue_records")
    void clearAll();

    // ---------- 查詢（同步/一次性取用） ----------
    /** 以有效時間由新到舊；一次性清單 */
    @Query("SELECT * FROM fatigue_records " +
            "ORDER BY (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) DESC")
    List<FatigueRecord> getAllDesc();

    /** 區間查詢（回傳由早到晚，畫折線自然）；一次性清單 */
    @Query("SELECT * FROM fatigue_records " +
            "WHERE (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) " +
            "BETWEEN :start AND :end " +
            "ORDER BY (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) ASC")
    List<FatigueRecord> getByTimeRange(long start, long end);

    // ---------- 查詢（UI 觀察用 LiveData；AdminActivity 直接用這兩個） ----------
    /** 以有效時間由新到舊；LiveData 會即時更新列表 */
    @Query("SELECT * FROM fatigue_records " +
            "ORDER BY (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) DESC")
    LiveData<List<FatigueRecord>> observeAll();

    /** 區間（由早到晚）；LiveData 會即時更新列表與圖表 */
    @Query("SELECT * FROM fatigue_records " +
            "WHERE (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) " +
            "BETWEEN :start AND :end " +
            "ORDER BY (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) ASC")
    LiveData<List<FatigueRecord>> observeBetween(long start, long end);

    // ---------- ✅ 給 Repository / Compose 用的 Flow ----------
    /** 最近 N 筆（以有效時間由新到舊）；Flow 版，Compose 會自動跟著更新 */
    @Query("SELECT * FROM fatigue_records " +
            "ORDER BY (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) DESC " +
            "LIMIT :limit")
    Flow<List<FatigueRecord>> recentFlow(int limit);

    /** 最近 N 筆（以有效時間由新到舊）；一次性清單備用 */
    @Query("SELECT * FROM fatigue_records " +
            "ORDER BY (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) DESC " +
            "LIMIT :limit")
    List<FatigueRecord> recent(int limit);

    // ---------- 統計（可選） ----------
    @Query("SELECT COUNT(*) FROM fatigue_records")
    int count();

    @Query("SELECT AVG(score) FROM fatigue_records")
    Float avgScore();

    @Query("SELECT AVG(score) FROM fatigue_records " +
            "WHERE (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) BETWEEN :start AND :end")
    Float avgScoreBetween(long start, long end);

    @Query("SELECT MAX(score) FROM fatigue_records " +
            "WHERE (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) BETWEEN :start AND :end")
    Float maxScoreBetween(long start, long end);

    @Query("SELECT MIN(score) FROM fatigue_records " +
            "WHERE (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) BETWEEN :start AND :end")
    Float minScoreBetween(long start, long end);

    // ---------- 同步（UploadWorker 用） ----------
    /** 未同步清單（由早到晚） */
    @Query("SELECT * FROM fatigue_records " +
            "WHERE synced = 0 " +
            "ORDER BY (CASE WHEN detectedAt=0 THEN COALESCE(timestampMs,0) ELSE detectedAt END) ASC")
    List<FatigueRecord> getUnsynced();

    /** 單筆設為已同步並寫入 serverId */
    @Query("UPDATE fatigue_records SET synced = 1, serverId = :serverId WHERE id = :localId")
    int markSynced(long localId, String serverId);
}
