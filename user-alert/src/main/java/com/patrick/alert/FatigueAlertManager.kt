package com.patrick.alert

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.patrick.core.FatigueDetectionResult
import com.patrick.core.FatigueLevel
import com.patrick.core.FatigueDialogCallback
import com.patrick.core.FatigueUiCallback
import com.patrick.core.PerformanceMonitor

class FatigueAlertManager(private val context: Context) {

    private val performanceMonitor = PerformanceMonitor.getInstance(context)

    private var uiCallback: FatigueUiCallback? = null
    private var dialogCallback: FatigueDialogCallback? = null

    // ---- 系統警示聲 ----
    private var tone: ToneGenerator? = null
    private var isPlaying = false

    fun setUiCallback(callback: FatigueUiCallback) {
        uiCallback = callback
    }

    fun setDialogCallback(callback: FatigueDialogCallback) {
        dialogCallback = callback
    }

    private fun playWarningBeep() {
        if (isPlaying) return

        try {
            tone = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            tone?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 800) // 0.8 秒
            isPlaying = true

            // 0.9 秒後重置
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                isPlaying = false
            }, 900)

        } catch (e: Exception) {
            Log.e("FatigueAlert", "Tone 播放失敗", e)
            isPlaying = false
        }
    }

    private fun stopBeep() {
        tone?.release()
        tone = null
        isPlaying = false
    }

    fun onUserAcknowledged() {
        stopBeep()
        dialogCallback?.onUserAcknowledged()
        uiCallback?.setWarningDialogActive(false)
    }

    fun onUserRequestedRest() {
        Log.e("FATIGUE_REST", "⚠ FatigueAlertManager.onUserRequestedRest() 被按到了")
        stopBeep()
        dialogCallback?.onUserRequestedRest()
        uiCallback?.setWarningDialogActive(false)
    }


    fun handleFatigueDetection(result: FatigueDetectionResult) {
        if (!result.isFatigueDetected) return

        when (result.fatigueLevel) {
            FatigueLevel.NOTICE -> {
                uiCallback?.onNoticeFatigue()
            }

            FatigueLevel.WARNING -> {
                uiCallback?.onWarningFatigue()
                playWarningBeep()   // ⭐ 在 WARNING 播警示音
            }
            else -> {}
        }
    }

    fun stopAllAlerts() {
        stopBeep()
        uiCallback?.setWarningDialogActive(false)
    }

    fun cleanup() {
        stopAllAlerts()
    }
}
