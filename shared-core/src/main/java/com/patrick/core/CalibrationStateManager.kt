package com.patrick.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * 校正狀態管理器
 * 負責管理校正狀態的持久化存儲
 * 確保校正只在程式啟動時執行一次
 */
class CalibrationStateManager(context: Context) {
    companion object {
        private const val TAG = "CalibrationStateManager"
        private const val PREF_NAME = "calibration_state"
        private const val KEY_HAS_CALIBRATED = "has_calibrated"
        private const val KEY_CALIBRATION_TIMESTAMP = "calibration_timestamp"
        private const val KEY_APP_SESSION_ID = "app_session_id"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE,
    )

    private var currentSessionId: String = generateSessionId()

    /**
     * 生成會話 ID
     */
    private fun generateSessionId(): String {
        return System.currentTimeMillis().toString()
    }

    /**
     * 檢查是否已經完成校正
     */
    fun hasCalibrated(): Boolean {
        val calibrated = sharedPreferences.getBoolean(KEY_HAS_CALIBRATED, false)
        val sessionId = sharedPreferences.getString(KEY_APP_SESSION_ID, null)
        
        Log.d(TAG, "檢查校正狀態: calibrated=$calibrated, sessionId=$sessionId, currentSessionId=$currentSessionId")
        
        // 只有在當前會話中完成校正才返回 true
        return calibrated && sessionId == currentSessionId
    }

    /**
     * 標記校正已完成
     */
    fun markCalibrationCompleted() {
        val timestamp = System.currentTimeMillis()
        sharedPreferences.edit()
            .putBoolean(KEY_HAS_CALIBRATED, true)
            .putLong(KEY_CALIBRATION_TIMESTAMP, timestamp)
            .putString(KEY_APP_SESSION_ID, currentSessionId)
            .apply()

        Log.d(TAG, "校正已完成並保存: timestamp=$timestamp, sessionId=$currentSessionId")
    }

    /**
     * 重置校正狀態（僅在程式完全關閉時調用）
     */
    fun resetCalibrationState() {
        sharedPreferences.edit()
            .remove(KEY_HAS_CALIBRATED)
            .remove(KEY_CALIBRATION_TIMESTAMP)
            .remove(KEY_APP_SESSION_ID)
            .apply()

        Log.d(TAG, "校正狀態已重置")
    }

    /**
     * 獲取校正時間戳
     */
    fun getCalibrationTimestamp(): Long {
        return sharedPreferences.getLong(KEY_CALIBRATION_TIMESTAMP, 0L)
    }

    /**
     * 獲取當前會話 ID
     */
    fun getCurrentSessionId(): String {
        return currentSessionId
    }

    /**
     * 檢查校正是否在當前會話中完成
     */
    fun isCalibratedInCurrentSession(): Boolean {
        val sessionId = sharedPreferences.getString(KEY_APP_SESSION_ID, null)
        return sessionId == currentSessionId
    }

    /**
     * 獲取校正狀態的詳細信息
     */
    fun getCalibrationStatusInfo(): String {
        val hasCalibrated = sharedPreferences.getBoolean(KEY_HAS_CALIBRATED, false)
        val timestamp = sharedPreferences.getLong(KEY_CALIBRATION_TIMESTAMP, 0L)
        val sessionId = sharedPreferences.getString(KEY_APP_SESSION_ID, null)
        val isCurrentSession = sessionId == currentSessionId

        return buildString {
            appendLine("校正狀態信息:")
            appendLine("- 已校正: $hasCalibrated")
            appendLine("- 校正時間: ${if (timestamp > 0) java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(timestamp)) else "未校正"}")
            appendLine("- 會話ID: $sessionId")
            appendLine("- 當前會話ID: $currentSessionId")
            appendLine("- 當前會話校正: $isCurrentSession")
            appendLine("- 有效校正: ${hasCalibrated()}")
        }
    }
} 