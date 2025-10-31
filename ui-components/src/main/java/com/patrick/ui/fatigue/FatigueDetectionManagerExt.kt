package com.patrick.ui.fatigue

import com.patrick.detection.FatigueDetectionManager

/**
 * 以 FPS 設定影像處理頻率（1~60）
 * 轉成「每幀最小處理間隔」呼叫既有的 setMinProcessIntervalMs。
 */
fun FatigueDetectionManager.setProcessingRateFps(fps: Int) {
    val clamped = fps.coerceIn(1, 60)
    this.setMinProcessIntervalMs(1000L / clamped)
}
