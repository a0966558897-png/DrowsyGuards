package com.example.drivesafe.db;


/** 對應你後端 /driving_records 的資料格式 */
public class DrivingRecordDto {
    public Integer id;              // 伺服器可能回傳
    public int member_id;           // 必填：會員整數 ID
    public String start_time;       // ISO8601，如 "2025-09-28T08:00:00Z"
    public String end_time;         // ISO8601
    public String location_start;   // 起點文字
    public String location_end;     // 終點文字
    public String fatigue_level;    // 例如 "HIGH" / "LOW"（字串）
    public boolean fatigue_detected;// 是否偵測到疲勞
}
