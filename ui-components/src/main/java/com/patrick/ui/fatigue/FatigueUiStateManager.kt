package com.patrick.ui.fatigue

import android.util.Log
import com.patrick.core.FatigueLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 疲勞 UI 狀態管理器
 * 負責處理 UI 相關的狀態管理，包括重置保護期和冷卻期
 */
class FatigueUiStateManager : com.patrick.core.FatigueUiCallback {
    companion object {
        private const val TAG = "FatigueUiStateManager"
    }

    // UI 狀態
    private val _currentFatigueLevel = MutableStateFlow(FatigueLevel.NORMAL)
    val currentFatigueLevel: StateFlow<FatigueLevel> = _currentFatigueLevel

    // 重置保護期
    private var isInResetProtection = false
    private var resetProtectionStartTime: Long = 0
    private val resetProtectionDuration = 10000L // 10 秒重置保護期

    // 冷卻期
    private var isInCooldownPeriod = false
    private var cooldownStartTime: Long = 0
    private val cooldownDuration = 8000L // 8 秒冷卻期

    // 對話框狀態
    private var hasActiveWarningDialog = false

    init {
        // 確保初始化時狀態正常
        reset()
        Log.d(TAG, "FatigueUiStateManager 初始化完成，狀態已重置")
    }

    /**
     * 處理疲勞檢測結果
     */
    fun processFatigueResult(
        rawFatigueLevel: FatigueLevel,
        fatigueEventCount: Int,
        currentTime: Long = System.currentTimeMillis(),
    ): FatigueLevel {
        Log.d(
            TAG,
            "處理疲勞結果: rawLevel=$rawFatigueLevel, eventCount=$fatigueEventCount, resetProtection=$isInResetProtection, cooldown=$isInCooldownPeriod",
        )

        // 檢查重置保護期
        if (isInResetProtection) {
            val protectionElapsed = currentTime - resetProtectionStartTime
            if (protectionElapsed >= resetProtectionDuration) {
                isInResetProtection = false
                Log.d(TAG, "重置保護期結束，恢復正常偵測")
                // 重置保護期結束後，繼續檢查冷卻期
            } else {
                Log.d(TAG, "重置保護期中，返回 NORMAL (${protectionElapsed}ms/${resetProtectionDuration}ms)")
                _currentFatigueLevel.value = FatigueLevel.NORMAL
                return FatigueLevel.NORMAL
            }
        }

        // 檢查冷卻期（重置保護期結束後才檢查）
        if (isInCooldownPeriod) {
            val cooldownElapsed = currentTime - cooldownStartTime
            if (cooldownElapsed >= cooldownDuration) {
                isInCooldownPeriod = false
                Log.d(TAG, "冷卻期結束，恢復正常警告觸發")
            } else {
                // 冷卻期中，將 WARNING 降級為 NOTICE，其他級別保持不變
                if (rawFatigueLevel == FatigueLevel.WARNING) {
                    Log.d(TAG, "冷卻期中，將 WARNING 降級為 NOTICE (${cooldownElapsed}ms/${cooldownDuration}ms)")
                    _currentFatigueLevel.value = FatigueLevel.NOTICE
                    return FatigueLevel.NOTICE
                } else {
                    Log.d(TAG, "冷卻期中，保持當前級別: $rawFatigueLevel (${cooldownElapsed}ms/${cooldownDuration}ms)")
                    _currentFatigueLevel.value = rawFatigueLevel
                    return rawFatigueLevel
                }
            }
        }

        // 檢查是否有活躍的警告對話框
        if (rawFatigueLevel == FatigueLevel.WARNING && hasActiveWarningDialog) {
            Log.d(TAG, "已有活躍的警告對話框，跳過重複觸發")
            return _currentFatigueLevel.value
        }

        // 更新狀態
        _currentFatigueLevel.value = rawFatigueLevel
        Log.d(TAG, "正常處理疲勞結果: $rawFatigueLevel (無保護期和冷卻期)")
        return rawFatigueLevel
    }

    /**
     * 用戶確認已清醒
     */
    override fun onUserAcknowledged() {
        Log.d(TAG, "用戶確認已清醒，啟動重置保護期和冷卻期")

        // 啟動重置保護期
        isInResetProtection = true
        resetProtectionStartTime = System.currentTimeMillis()

        // 啟動冷卻期
        isInCooldownPeriod = true
        cooldownStartTime = System.currentTimeMillis()

        // 重置對話框狀態
        hasActiveWarningDialog = false

        // 更新 UI 狀態
        _currentFatigueLevel.value = FatigueLevel.NORMAL

        Log.d(TAG, "重置狀態已啟動: resetProtection=$isInResetProtection, cooldown=$isInCooldownPeriod")
    }

    /**
     * 用戶要求休息
     */
    override fun onUserRequestedRest() {
        Log.d(TAG, "用戶要求休息")

        // 啟動重置保護期
        isInResetProtection = true
        resetProtectionStartTime = System.currentTimeMillis()

        // 啟動冷卻期
        isInCooldownPeriod = true
        cooldownStartTime = System.currentTimeMillis()

        // 重置對話框狀態
        hasActiveWarningDialog = false

        // 更新 UI 狀態
        _currentFatigueLevel.value = FatigueLevel.NORMAL

        Log.d(TAG, "休息狀態已啟動: resetProtection=$isInResetProtection, cooldown=$isInCooldownPeriod")
    }

    override fun setWarningDialogActive(active: Boolean) {
        hasActiveWarningDialog = active
        Log.d(TAG, "警告對話框狀態: $active (FatigueUiCallback)")
    }

    /**
     * 檢查是否有活躍的警告對話框
     */
    fun hasActiveWarningDialog(): Boolean = hasActiveWarningDialog

    /**
     * 檢查是否在重置保護期內
     */
    fun isInResetProtection(): Boolean = isInResetProtection

    /**
     * 檢查是否在冷卻期內
     */
    fun isInCooldownPeriod(): Boolean = isInCooldownPeriod

    /**
     * 獲取重置狀態信息（用於調試）
     */
    fun getResetStatusInfo(): String {
        val currentTime = System.currentTimeMillis()
        val protectionElapsed = if (isInResetProtection) currentTime - resetProtectionStartTime else 0L
        val cooldownElapsed = if (isInCooldownPeriod) currentTime - cooldownStartTime else 0L

        return "ResetProtection: $isInResetProtection (${protectionElapsed}ms/${resetProtectionDuration}ms), " +
            "Cooldown: $isInCooldownPeriod (${cooldownElapsed}ms/${cooldownDuration}ms), " +
            "Dialog: $hasActiveWarningDialog, " +
            "Level: ${_currentFatigueLevel.value}"
    }

    /**
     * 重置所有狀態
     */
    fun reset() {
        isInResetProtection = false
        isInCooldownPeriod = false
        hasActiveWarningDialog = false
        _currentFatigueLevel.value = FatigueLevel.NORMAL
        Log.d(TAG, "所有狀態已重置")
    }

    override fun onBlink() {
        // 眨眼事件暫時不處理
    }
}
