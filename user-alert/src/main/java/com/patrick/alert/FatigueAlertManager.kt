package com.patrick.alert

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.patrick.core.FatigueDetectionResult
import com.patrick.core.FatigueDialogCallback
import com.patrick.core.FatigueLevel
import com.patrick.core.FatigueUiCallback
import com.patrick.core.PerformanceMonitor

class FatigueAlertManager(private val context: Context) {

    companion object {
        private const val TAG = "FatigueAlertManager"
    }

    private val performanceMonitor = PerformanceMonitor.getInstance(context)

    private var uiCallback: FatigueUiCallback? = null
    private var dialogCallback: FatigueDialogCallback? = null

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    fun setUiCallback(callback: FatigueUiCallback) {
        uiCallback = callback
    }

    fun setDialogCallback(callback: FatigueDialogCallback) {
        dialogCallback = callback
    }

    // ⭐ 不用 R，改用 resource identifier
    private fun playWarningSound() {
        if (isPlaying) return

        try { mediaPlayer?.release() } catch (_: Exception) {}

        // ⭐ 用字串抓 raw 資源，不用跨模組 import R
        val resId = context.resources.getIdentifier("emergency", "raw", context.packageName)

        if (resId == 0) {
            Log.e(TAG, "找不到 raw/emergency.wav")
            return
        }

        mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer?.isLooping = false
        mediaPlayer?.start()

        isPlaying = true

        mediaPlayer?.setOnCompletionListener {
            isPlaying = false
        }

        Log.d(TAG, "正在播放 emergency.wav")
    }

    private fun stopSound() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}

        mediaPlayer = null
        isPlaying = false
    }

    fun onUserAcknowledged() {
        stopSound()
        dialogCallback?.onUserAcknowledged()
        uiCallback?.setWarningDialogActive(false)
    }

    fun onUserRequestedRest() {
        stopSound()
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
                playWarningSound()
            }
            else -> {}
        }
    }

    fun stopAllAlerts() {
        stopSound()
        uiCallback?.setWarningDialogActive(false)
    }

    fun cleanup() {
        stopAllAlerts()
    }
}
