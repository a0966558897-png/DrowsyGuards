package com.patrick.alert

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.patrick.core.FatigueDetectionResult
import com.patrick.core.FatigueLevel

/**
 * 視覺警報管理器
 * 負責處理 Toast 消息和 TextView 警報顯示
 */
class VisualAlertManager(private val context: Context) {
    companion object {
        const val ALERT_DURATION_MS = 3000L // 警報顯示3秒
    }

    private val alertHandler = Handler(Looper.getMainLooper())

    // 警報文本
    private val alertMessages =
        mapOf(
            FatigueLevel.NOTICE to "⚠️ 檢測到疲勞跡象，請注意安全！",
            FatigueLevel.WARNING to "🚨 疲勞警告！請立即確認狀態！",
        )

    /**
     * 顯示 Toast 消息
     */
    fun showToastMessage(message: String) {
        alertHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 在 TextView 上顯示警報消息
     */
    fun showAlertOnTextView(
        textView: TextView,
        result: FatigueDetectionResult,
    ) {
        if (!result.isFatigueDetected) {
            textView.visibility = View.GONE
            return
        }

        val message = alertMessages[result.fatigueLevel] ?: ""
        alertHandler.post {
            textView.text = message
            textView.visibility = View.VISIBLE

            // 設置文本顏色
            textView.setTextColor(
                when (result.fatigueLevel) {
                    FatigueLevel.NOTICE -> android.graphics.Color.parseColor("#FFA500") // 橙色
                    FatigueLevel.WARNING -> android.graphics.Color.parseColor("#FF0000") // 紅色
                    else -> android.graphics.Color.BLACK
                },
            )
        }

        // 3秒后隱藏消息
        alertHandler.postDelayed({
            textView.visibility = View.GONE
        }, ALERT_DURATION_MS)
    }

    /**
     * 停止所有視覺警報
     */
    fun stopAllVisualAlerts() {
        alertHandler.removeCallbacksAndMessages(null)
    }
}
