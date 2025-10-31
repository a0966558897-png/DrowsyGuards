package com.patrick.core

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 疲勞偵測調試工具
 * 提供調試功能，包括參數調整、日誌管理和報告生成
 */
class FatigueDetectionDebugger(private val context: Context) {
    companion object {
        private const val TAG = "FatigueDetectionDebugger"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    }

    /**
     * 調試配置
     */
    data class DebugConfig(
        val enableSensitivityLog: Boolean = true,
        val enableTriggerLog: Boolean = true,
        val enableCalibrationLog: Boolean = true,
        val enableEventLog: Boolean = true,
        val enableResetLog: Boolean = true,
        val logToFile: Boolean = false,
        val autoGenerateReport: Boolean = false,
        val reportInterval: Long = 60000L, // 1分鐘
    )

    private var debugConfig = DebugConfig()
    private var lastReportTime = 0L

    /**
     * 設置調試配置
     */
    fun setDebugConfig(config: DebugConfig) {
        debugConfig = config
        FatigueDetectionLogger.setLogEnabled(
            sensitivity = config.enableSensitivityLog,
            trigger = config.enableTriggerLog,
            calibration = config.enableCalibrationLog,
            event = config.enableEventLog,
            reset = config.enableResetLog,
        )
        Log.d(TAG, "調試配置已更新: $config")
    }

    /**
     * 快速設置日誌開關
     */
    fun setLogEnabled(
        sensitivity: Boolean = true,
        trigger: Boolean = true,
        calibration: Boolean = true,
        event: Boolean = true,
        reset: Boolean = true,
    ) {
        debugConfig =
            debugConfig.copy(
                enableSensitivityLog = sensitivity,
                enableTriggerLog = trigger,
                enableCalibrationLog = calibration,
                enableEventLog = event,
                enableResetLog = reset,
            )
        FatigueDetectionLogger.setLogEnabled(sensitivity, trigger, calibration, event, reset)
    }

    /**
     * 生成調試報告
     */
    fun generateDebugReport(
        parameters: Map<String, Any>,
        sensitivityReport: String,
        uiStateInfo: String,
    ): String {
        val timestamp = dateFormat.format(Date())
        val sb = StringBuilder()

        sb.appendLine("=== 疲勞偵測調試報告 ===")
        sb.appendLine("生成時間: $timestamp")
        sb.appendLine()

        // 檢測參數
        sb.appendLine("=== 檢測參數 ===")
        parameters.forEach { (key, value) ->
            sb.appendLine("  $key: $value")
        }
        sb.appendLine()

        // 靈敏度報告
        sb.appendLine(sensitivityReport)

        // UI 狀態信息
        sb.appendLine("=== UI 狀態信息 ===")
        sb.appendLine("  $uiStateInfo")
        sb.appendLine()

        // 調試配置
        sb.appendLine("=== 調試配置 ===")
        sb.appendLine("  靈敏度日誌: ${debugConfig.enableSensitivityLog}")
        sb.appendLine("  觸發日誌: ${debugConfig.enableTriggerLog}")
        sb.appendLine("  校正日誌: ${debugConfig.enableCalibrationLog}")
        sb.appendLine("  事件日誌: ${debugConfig.enableEventLog}")
        sb.appendLine("  重置日誌: ${debugConfig.enableResetLog}")
        sb.appendLine("  文件日誌: ${debugConfig.logToFile}")
        sb.appendLine("  自動報告: ${debugConfig.autoGenerateReport}")
        sb.appendLine()

        // 調整建議
        sb.appendLine("=== 調整建議 ===")
        generateAdjustmentSuggestions(parameters, sb)

        return sb.toString()
    }

    /**
     * 生成調整建議
     */
    private fun generateAdjustmentSuggestions(
        parameters: Map<String, Any>,
        sb: StringBuilder,
    ) {
        val earThreshold = parameters["earThreshold"] as? Float ?: 0f
        val fatigueEventThreshold = parameters["fatigueEventThreshold"] as? Int ?: 1

        sb.appendLine("1. EAR 閾值調整:")
        if (earThreshold > 0.2f) {
            sb.appendLine("   - 當前閾值較高，可能導致漏檢，建議降低到 ${earThreshold * 0.8f}")
        } else if (earThreshold < 0.1f) {
            sb.appendLine("   - 當前閾值較低，可能導致誤檢，建議提高到 ${earThreshold * 1.2f}")
        } else {
            sb.appendLine("   - 當前閾值在合理範圍內")
        }

        sb.appendLine("2. 疲勞事件閾值調整:")
        if (fatigueEventThreshold > 3) {
            sb.appendLine("   - 當前閾值較高，警告觸發較難，建議降低到 ${fatigueEventThreshold - 1}")
        } else if (fatigueEventThreshold < 1) {
            sb.appendLine("   - 當前閾值較低，警告觸發較易，建議提高到 ${fatigueEventThreshold + 1}")
        } else {
            sb.appendLine("   - 當前閾值在合理範圍內")
        }

        sb.appendLine("3. 時間閾值調整:")
        val earClosureThreshold = parameters["earClosureDurationThreshold"] as? Long ?: 2000L
        if (earClosureThreshold > 3000L) {
            sb.appendLine("   - 眼睛閉合時間閾值較長，建議縮短到 ${earClosureThreshold - 500}ms")
        } else if (earClosureThreshold < 1000L) {
            sb.appendLine("   - 眼睛閉合時間閾值較短，建議延長到 ${earClosureThreshold + 500}ms")
        } else {
            sb.appendLine("   - 眼睛閉合時間閾值在合理範圍內")
        }
    }

    /**
     * 保存調試報告到文件
     */
    fun saveDebugReport(
        report: String,
        filename: String? = null,
    ): String {
        val timestamp = dateFormat.format(Date())
        val reportFilename = filename ?: "fatigue_debug_report_$timestamp.txt"

        try {
            val file = File(context.getExternalFilesDir(null), reportFilename)
            file.writeText(report)
            Log.d(TAG, "調試報告已保存: ${file.absolutePath}")
            return file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "保存調試報告失敗", e)
            return ""
        }
    }

    /**
     * 檢查是否需要自動生成報告
     */
    fun checkAutoGenerateReport(
        parameters: Map<String, Any>,
        sensitivityReport: String,
        uiStateInfo: String,
    ): String? {
        if (!debugConfig.autoGenerateReport) return null

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastReportTime >= debugConfig.reportInterval) {
            lastReportTime = currentTime
            val report = generateDebugReport(parameters, sensitivityReport, uiStateInfo)

            if (debugConfig.logToFile) {
                saveDebugReport(report)
            }

            return report
        }

        return null
    }

    /**
     * 快速調試模式設置
     */
    fun enableQuickDebugMode() {
        setDebugConfig(
            DebugConfig(
                enableSensitivityLog = true,
                enableTriggerLog = true,
                enableCalibrationLog = true,
                enableEventLog = true,
                enableResetLog = true,
                logToFile = true,
                autoGenerateReport = true,
                reportInterval = 30000L, // 30秒
            ),
        )
        Log.d(TAG, "快速調試模式已啟用")
    }

    /**
     * 性能調試模式設置
     */
    fun enablePerformanceDebugMode() {
        setDebugConfig(
            DebugConfig(
                enableSensitivityLog = false,
                enableTriggerLog = true,
                enableCalibrationLog = false,
                enableEventLog = false,
                enableResetLog = false,
                logToFile = false,
                autoGenerateReport = false,
            ),
        )
        Log.d(TAG, "性能調試模式已啟用")
    }

    /**
     * 靈敏度調試模式設置
     */
    fun enableSensitivityDebugMode() {
        setDebugConfig(
            DebugConfig(
                enableSensitivityLog = true,
                enableTriggerLog = false,
                enableCalibrationLog = true,
                enableEventLog = false,
                enableResetLog = false,
                logToFile = true,
                autoGenerateReport = true,
                reportInterval = 60000L, // 1分鐘
            ),
        )
        Log.d(TAG, "靈敏度調試模式已啟用")
    }

    /**
     * 關閉所有調試日誌
     */
    fun disableAllLogs() {
        setDebugConfig(
            DebugConfig(
                enableSensitivityLog = false,
                enableTriggerLog = false,
                enableCalibrationLog = false,
                enableEventLog = false,
                enableResetLog = false,
                logToFile = false,
                autoGenerateReport = false,
            ),
        )
        Log.d(TAG, "所有調試日誌已關閉")
    }
} 
