package com.patrick.alert

import android.util.Log
import android.widget.TextView
import android.content.Context
import com.patrick.core.AsyncTaskManager
import com.patrick.core.FatigueDetectionResult
import com.patrick.core.FatigueLevel
import com.patrick.core.PerformanceMonitor
import com.patrick.core.FatigueDialogCallback
import com.patrick.core.FatigueUiCallback

/**
 * 疲勞提醒管理器
 * 僅負責分發警告事件與橋接 UI 顯示狀態，不直接操作 Dialog 或持有 Activity 參考
 */
class FatigueAlertManager(private val context: Context) {
    companion object {
        private const val TAG = "FatigueAlertManager"
    }

    private val performanceMonitor = PerformanceMonitor.getInstance(context)

    private var uiCallback: FatigueUiCallback? = null
    private var dialogCallback: FatigueDialogCallback? = null

    fun setUiCallback(callback: FatigueUiCallback) {
        this.uiCallback = callback
    }

    fun setDialogCallback(callback: FatigueDialogCallback) {
        this.dialogCallback = callback
    }

    /** 由 UI 按鈕觸發：使用者確認已清醒 */
    fun onUserAcknowledged() {
        // 通知業務邏輯重置
        dialogCallback?.onUserAcknowledged()
        // 保險起見，同步關閉對話框
        uiCallback?.setWarningDialogActive(false)
    }

    /** 由 UI 按鈕觸發：使用者要求休息 */
    fun onUserRequestedRest() {
        // 通知業務邏輯進入休息流程
        dialogCallback?.onUserRequestedRest()
        // 保險起見，同步關閉對話框
        uiCallback?.setWarningDialogActive(false)
    }

    /**
     * 由偵測結果觸發：分發疲勞等級事件
     * 注意：是否顯示對話框，交給 DetectionManager/VM 透過 setWarningDialogActive 控制
     */
    fun handleFatigueDetection(result: FatigueDetectionResult) {
        performanceMonitor.logPerformance(
            "Fatigue detection handled",
            mapOf(
                "fatigueLevel" to result.fatigueLevel.name,
                "isFatigueDetected" to result.isFatigueDetected,
            ),
        )

        if (!result.isFatigueDetected) return

        Log.d(TAG, "檢測到疲勞，級別: ${result.fatigueLevel}")

        when (result.fatigueLevel) {
            FatigueLevel.NOTICE -> {
                uiCallback?.onNoticeFatigue()
                // NOTICE 不強制開對話框
            }
            FatigueLevel.WARNING -> {
                uiCallback?.onWarningFatigue()
                // 交由上層（DetectionManager/VM）決定是否 setWarningDialogActive(true)
            }
            else -> {}
        }
    }

    /** 可選：保留原先的文字提醒邏輯（目前不使用） */
    fun showAlertOnTextView(
        @Suppress("UNUSED_PARAMETER") textView: TextView,
        @Suppress("UNUSED_PARAMETER") result: FatigueDetectionResult,
    ) {
        // 視覺顯示由 Compose/對話框處理；此方法留作兼容
    }

    /** 供上層主動關閉所有提醒時呼叫（關閉聲音/震動/對話框狀態等） */
    fun stopAllAlerts() {
        Log.d(TAG, "停止所有警報")
        // 關閉對話框狀態
        uiCallback?.setWarningDialogActive(false)
        // 若之後新增聲音/震動，也在此停止
    }

    fun cleanup() {
        stopAllAlerts()
    }
}
