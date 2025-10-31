package com.patrick.core

/**
 * 疲劳检测结果
 */
data class FatigueDetectionResult(
    val isFatigueDetected: Boolean,
    val fatigueLevel: FatigueLevel,
    val events: List<FatigueEvent>,
    val faceDetected: Boolean = true, // 是否偵測到臉部
)

/**
 * 疲劳级别
 */
enum class FatigueLevel {
    NORMAL, // 正常
    NOTICE, // 提醒（原 MODERATE）
    WARNING, // 警告（原 SEVERE）
}

/**
 * 疲劳事件
 */
sealed class FatigueEvent {
    data class EyeClosure(val duration: Long) : FatigueEvent()

    data class Yawn(val duration: Long) : FatigueEvent()

    data class HighBlinkFrequency(val frequency: Int) : FatigueEvent()
}

/**
 * 疲劳检测监听器
 */
interface FatigueDetectionListener {
    fun onFatigueDetected(result: FatigueDetectionResult)

    fun onFatigueLevelChanged(level: FatigueLevel)

    fun onBlink()

    fun onCalibrationStarted()

    fun onCalibrationProgress(
        progress: Int,
        currentEar: Float,
    )

    fun onCalibrationCompleted(
        newThreshold: Float,
        minEar: Float,
        maxEar: Float,
        avgEar: Float,
    )
}

/**
 * 疲劳UI回调接口
 */
interface FatigueUiCallback {
    fun onNormalDetection() {}
    fun onNoFaceDetected() {}
    fun onNoticeFatigue() {}
    fun onWarningFatigue() {}
    fun onUserAcknowledged() {}
    fun onUserRequestedRest() {}
    fun onBlink() {}
    fun onCalibrationStarted() {}
    fun onCalibrationProgress(progress: Int, currentEar: Float) {}
    fun onCalibrationCompleted(newThreshold: Float, minEar: Float, maxEar: Float, avgEar: Float) {}
    fun setWarningDialogActive(active: Boolean)
    fun onFatigueScoreUpdated(score: Int, level: FatigueLevel) {}
}
