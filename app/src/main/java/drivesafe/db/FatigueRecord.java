package com.example.drivesafe.db;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 疲勞紀錄實體：
 * - userId: 關聯 users 表
 * - detectedAt：新時間欄位（毫秒，優先）
 * - timestampMs：舊欄位（毫秒，可為 null）
 * - effectiveTime()：統一對外時間
 *
 * UI 相容用 getter/setter：fatigueLevel / sourceDevice / blinks / yawns / eyesClosedMs
 */
@Entity(tableName = "fatigue_records")
public class FatigueRecord {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    /** 使用者 ID，連到 users.id */
    @ColumnInfo(name = "userId")
    private long userId;

    @ColumnInfo(name = "serverId")
    @Nullable
    private String serverId;

    @ColumnInfo(name = "detectedAt")
    private long detectedAt; // 0 表示未設定

    @ColumnInfo(name = "timestampMs")
    @Nullable
    private Long timestampMs; // 舊欄位

    @ColumnInfo(name = "score")
    private float score;

    @ColumnInfo(name = "synced")
    private boolean synced;

    // ==== 建構子 ====
    public FatigueRecord() {}

    /** 建新資料建議用這個建構子（新舊時間欄位對齊） */
    public FatigueRecord(long detectedAt, float score) {
        this.detectedAt = detectedAt;
        this.timestampMs = detectedAt;
        this.score = score;
        this.synced = false;
        this.userId = 0; // 預設 0（未知使用者）
    }

    /** 工廠方法：以現在時間建立一筆紀錄 */
    public static FatigueRecord now(float score) {
        return new FatigueRecord(System.currentTimeMillis(), score);
    }

    // ==== 小工具方法 ====
    public long effectiveTime() {
        if (detectedAt != 0) return detectedAt;
        return timestampMs != null ? timestampMs : 0L;
    }

    public void setBothTimes(long tsMillis) {
        this.detectedAt = tsMillis;
        this.timestampMs = tsMillis;
    }

    public long safeTimestampMs() {
        return timestampMs != null ? timestampMs : 0L;
    }

    public void alignTimesIfMissing() {
        if (detectedAt == 0 && timestampMs != null && timestampMs > 0) {
            detectedAt = timestampMs;
        }
        if ((timestampMs == null || timestampMs == 0) && detectedAt > 0) {
            timestampMs = detectedAt;
        }
    }

    // ==== 相容用 Getter/Setter（不入庫）====
    public @Nullable String getFatigueLevel() { return null; }
    public void setFatigueLevel(@Nullable String v) { }

    public @Nullable String getSourceDevice() { return null; }
    public void setSourceDevice(@Nullable String v) { }

    public int getBlinks() { return 0; }
    public void setBlinks(int v) { }

    public int getYawns() { return 0; }
    public void setYawns(int v) { }

    public int getEyesClosedMs() { return 0; }
    public void setEyesClosedMs(int v) { }

    // ==== Getter / Setter ====
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public @Nullable String getServerId() { return serverId; }
    public void setServerId(@Nullable String serverId) { this.serverId = serverId; }

    public long getDetectedAt() { return detectedAt; }
    public void setDetectedAt(long detectedAt) { this.detectedAt = detectedAt; }

    public @Nullable Long getTimestampMs() { return timestampMs; }
    public void setTimestampMs(@Nullable Long timestampMs) { this.timestampMs = timestampMs; }

    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    @NonNull
    @Override
    public String toString() {
        return "FatigueRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", serverId=" + serverId +
                ", detectedAt=" + detectedAt +
                ", timestampMs=" + timestampMs +
                ", score=" + score +
                ", synced=" + synced +
                '}';
    }
}
