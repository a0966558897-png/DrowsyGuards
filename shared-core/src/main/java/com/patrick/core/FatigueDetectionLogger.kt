package com.patrick.core

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 疲勞偵測日誌管理器
 * 提供結構化的日誌記錄，便於調試疲勞偵測的靈敏度和觸發邏輯
 */
object FatigueDetectionLogger {
    private const val TAG = "FatigueDetection"
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    // 日誌開關
    private var isSensitivityLogEnabled = true
    private var isTriggerLogEnabled = true
    private var isCalibrationLogEnabled = true
    private var isEventLogEnabled = true
    private var isResetLogEnabled = true

    /**
     * 設置日誌開關
     */
    fun setLogEnabled(
        sensitivity: Boolean = true,
        trigger: Boolean = true,
        calibration: Boolean = true,
        event: Boolean = true,
        reset: Boolean = true,
    ) {
        isSensitivityLogEnabled = sensitivity
        isTriggerLogEnabled = trigger
        isCalibrationLogEnabled = calibration
        isEventLogEnabled = event
        isResetLogEnabled = reset
    }

    /**
     * 記錄靈敏度相關日誌
     */
    fun logSensitivity(
        message: String,
        earValue: Float? = null,
        marValue: Float? = null,
        earThreshold: Float? = null,
        marThreshold: Float? = null,
        isCritical: Boolean = false,
    ) {
        if (!isSensitivityLogEnabled) return

        val prefix = if (isCritical) "[CRITICAL]" else "[SENSITIVITY]"
        val timestamp = dateFormat.format(System.currentTimeMillis())
        val details = buildString {
            earValue?.let { append(" EAR=$it") }
            marValue?.let { append(" MAR=$it") }
            earThreshold?.let { append(" EAR_TH=$it") }
            marThreshold?.let { append(" MAR_TH=$it") }
        }

        val fullMessage = "$prefix [$timestamp] $message$details"
        if (isCritical) {
            Log.w(TAG, fullMessage)
        } else {
            Log.d(TAG, fullMessage)
        }
    }

    /**
     * 記錄觸發相關日誌
     */
    fun logTrigger(
        message: String,
        triggerType: String? = null,
        count: Int? = null,
        threshold: Int? = null,
        duration: Long? = null,
        isTriggered: Boolean = false,
    ) {
        if (!isTriggerLogEnabled) return

        val prefix = if (isTriggered) "[TRIGGERED]" else "[TRIGGER]"
        val timestamp = dateFormat.format(System.currentTimeMillis())
        val details = buildString {
            triggerType?.let { append(" TYPE=$it") }
            count?.let { append(" COUNT=$it") }
            threshold?.let { append(" THRESHOLD=$it") }
            duration?.let { append(" DURATION=${it}ms") }
        }

        val fullMessage = "$prefix [$timestamp] $message$details"
        if (isTriggered) {
            Log.i(TAG, fullMessage)
        } else {
            Log.d(TAG, fullMessage)
        }
    }

    /**
     * 記錄校正相關日誌
     */
    fun logCalibration(
        message: String,
        progress: Int? = null,
        currentEar: Float? = null,
        minEar: Float? = null,
        maxEar: Float? = null,
        avgEar: Float? = null,
        newThreshold: Float? = null,
        sampleCount: Int? = null,
    ) {
        if (!isCalibrationLogEnabled) return

        val timestamp = dateFormat.format(System.currentTimeMillis())
        val details = buildString {
            progress?.let { append(" PROGRESS=$it%") }
            currentEar?.let { append(" CURRENT_EAR=$it") }
            minEar?.let { append(" MIN_EAR=$it") }
            maxEar?.let { append(" MAX_EAR=$it") }
            avgEar?.let { append(" AVG_EAR=$it") }
            newThreshold?.let { append(" NEW_THRESHOLD=$it") }
            sampleCount?.let { append(" SAMPLES=$it") }
        }

        val fullMessage = "[CALIBRATION] [$timestamp] $message$details"
        Log.i(TAG, fullMessage)
    }

    /**
     * 記錄事件相關日誌
     */
    fun logEvent(
        message: String,
        eventType: String? = null,
        count: Int? = null,
        duration: Long? = null,
        level: String = "INFO",
    ) {
        if (!isEventLogEnabled) return

        val timestamp = dateFormat.format(System.currentTimeMillis())
        val details = buildString {
            eventType?.let { append(" TYPE=$it") }
            count?.let { append(" COUNT=$it") }
            duration?.let { append(" DURATION=${it}ms") }
        }

        val fullMessage = "[EVENT] [$timestamp] $message$details"
        when (level.uppercase()) {
            "WARN" -> Log.w(TAG, fullMessage)
            "ERROR" -> Log.e(TAG, fullMessage)
            else -> Log.i(TAG, fullMessage)
        }
    }

    /**
     * 記錄重置相關日誌
     */
    fun logReset(
        message: String,
        resetType: String? = null,
        previousCount: Int? = null,
        reason: String? = null,
    ) {
        if (!isResetLogEnabled) return

        val timestamp = dateFormat.format(System.currentTimeMillis())
        val details = buildString {
            resetType?.let { append(" TYPE=$it") }
            previousCount?.let { append(" PREV_COUNT=$it") }
            reason?.let { append(" REASON=$it") }
        }

        val fullMessage = "[RESET] [$timestamp] $message$details"
        Log.i(TAG, fullMessage)
    }

    /**
     * 生成分析報告
     */
    fun generateAnalysisReport(
        earValues: List<Float>,
        marValues: List<Float>,
        eventCounts: Map<String, Int>,
        calibrationData: Map<String, Float>?,
    ): String {
        val sb = StringBuilder()
        val timestamp = dateFormat.format(System.currentTimeMillis())

        sb.appendLine("=== 疲勞偵測分析報告 ===")
        sb.appendLine("生成時間: $timestamp")
        sb.appendLine()

        // EAR 統計
        sb.appendLine("=== EAR 統計 ===")
        if (earValues.isNotEmpty()) {
            val avgEar = earValues.average()
            val minEar = earValues.minOrNull()
            val maxEar = earValues.maxOrNull()
            sb.appendLine("樣本數: ${earValues.size}")
            sb.appendLine("平均值: ${"%.4f".format(avgEar)}")
            sb.appendLine("最小值: ${"%.4f".format(minEar)}")
            sb.appendLine("最大值: ${"%.4f".format(maxEar)}")
        } else {
            sb.appendLine("無 EAR 數據")
        }

        // MAR 統計
        sb.appendLine("\n=== MAR 統計 ===")
        if (marValues.isNotEmpty()) {
            val avgMar = marValues.average()
            val minMar = marValues.minOrNull()
            val maxMar = marValues.maxOrNull()
            sb.appendLine("樣本數: ${marValues.size}")
            sb.appendLine("平均值: ${"%.4f".format(avgMar)}")
            sb.appendLine("最小值: ${"%.4f".format(minMar)}")
            sb.appendLine("最大值: ${"%.4f".format(maxMar)}")
        } else {
            sb.appendLine("無 MAR 數據")
        }

        // 事件統計
        sb.appendLine("\n=== 事件統計 ===")
        if (eventCounts.isNotEmpty()) {
            eventCounts.forEach { (type, count) ->
                sb.appendLine("$type: $count 次")
            }
        } else {
            sb.appendLine("無事件記錄")
        }

        // 校正數據
        sb.appendLine("\n=== 校正數據 ===")
        if (!calibrationData.isNullOrEmpty()) {
            calibrationData.forEach { (key, value) ->
                sb.appendLine("$key: ${"%.4f".format(value)}")
            }
        } else {
            sb.appendLine("無校正數據")
        }

        // 建議
        sb.appendLine("\n=== 調整建議 ===")
        if (earValues.isNotEmpty()) {
            val avgEar = earValues.average().toFloat()
            val suggestedThreshold = avgEar * 0.8f
            sb.appendLine("建議 EAR 閾值: ${"%.4f".format(suggestedThreshold)} (基於平均值 ${"%.4f".format(avgEar)})")
        }

        return sb.toString()
    }
}
