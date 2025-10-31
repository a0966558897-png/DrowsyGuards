package com.example.drivesafe.db;


import androidx.annotation.Keep;

/**
 * 與後端(FastAPI)交換用的資料模型。
 * JSON 採 snake_case，直接對應後端欄位，不需要 @SerializedName。
 * 加上 @Keep 以避免未來開啟混淆時欄位被重新命名，導致 GSON 解析失敗。
 */
@Keep
public class FatigueDto {
    /** 伺服器建立後回傳的 UUID（新增時可為 null） */
    public String id;

    /** 使用者識別（與後端一致，snake_case） */
    public String user_id;

    /** 毫秒時間戳（建議用 UTC；查詢用 start_ms / end_ms 範圍） */
    public long timestamp_ms;

    /** 疲勞指數（1~10） */
    public float score;

    /** GSON 需要無參數建構子 */
    public FatigueDto() {}

    /** 方便手動建立完整物件 */
    public FatigueDto(String id, String user_id, long timestamp_ms, float score) {
        this.id = id;
        this.user_id = user_id;
        this.timestamp_ms = timestamp_ms;
        this.score = score;
    }

    /** 方便建立「新增」時要送出的物件（沒有 id） */
    public static FatigueDto of(String userId, long timestampMs, float score) {
        return new FatigueDto(null, userId, timestampMs, score);
    }

    @Override
    public String toString() {
        return "FatigueDto{id='" + id + "', user_id='" + user_id +
                "', timestamp_ms=" + timestamp_ms + ", score=" + score + "}";
    }
}
