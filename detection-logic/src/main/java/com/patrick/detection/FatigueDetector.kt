package com.patrick.detection

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.patrick.core.CalibrationStateManager
import com.patrick.core.FatigueDetectionListener
import com.patrick.core.FatigueDetectionLogger
import com.patrick.core.FatigueDetectionResult
import com.patrick.core.FatigueEvent
import com.patrick.core.FatigueLevel
import kotlin.math.sqrt

/**
 * 疲勞檢測器 - 核心疲勞檢測邏輯
 */
class FatigueDetector(private val context: Context) {

    companion object {
        private const val TAG = "FatigueDetector"

        // 閾值與條件（保留你的值）
        private const val DEFAULT_EAR_THRESHOLD = 0.15f
        private const val DEFAULT_MAR_THRESHOLD = 0.6f
        private const val DEFAULT_FATIGUE_EVENT_THRESHOLD = 2

        private const val DEFAULT_EAR_CLOSURE_DURATION_THRESHOLD = 1200L
        private const val DEFAULT_YAWN_DURATION_THRESHOLD = 2000L
        private const val DEFAULT_YAWN_MIN_DURATION = 1000L
        private const val DEFAULT_BLINK_FREQUENCY_THRESHOLD = 25

        private object LandmarkIndices {
            val LEFT_EYE = listOf(33, 160, 158, 133, 153, 144)
            val RIGHT_EYE = listOf(362, 385, 387, 263, 373, 380)
            val MOUTH = listOf(61, 84, 17, 314, 405, 320, 307, 375, 321, 308, 324, 318)
        }
    }

    // 參數
    private var currentEarThreshold = DEFAULT_EAR_THRESHOLD
    private var currentMarThreshold = DEFAULT_MAR_THRESHOLD
    private var currentFatigueEventThreshold = DEFAULT_FATIGUE_EVENT_THRESHOLD

    // 時間/計數
    private var lastEyeClosureStartTime: Long = 0
    private var lastMouthOpenStartTime: Long = 0
    private var lastBlinkTime: Long = 0

    private var fatigueEventCount = 0
    private var blinkCount = 0
    private var yawnCount = 0
    private var blinkFrequencyWarningCount = 0
    private var lastMinuteStartTime: Long = System.currentTimeMillis()

    // 眨眼時間戳（用於 Recent 計數）
    private val blinkTimestamps = mutableListOf<Long>()

    // ★ 嘴巴張開次數與時間戳
    private var mouthOpenCount = 0
    private val mouthOpenTimestamps = mutableListOf<Long>()

    // 狀態旗標
    private var isEyeClosed = false
    private var isMouthOpen = false

    // 校正
    private var isCalibrating = false
    private var calibrationStartTime: Long = 0
    private val calibrationEarValues = mutableListOf<Float>()
    private val calibrationDuration = 15000L
    private val calibrationStateManager = CalibrationStateManager(context)

    // 臉部偵測狀態（不要在沒臉時停止校正）
    private var isFaceDetected = false
    private var lastFaceDetectionTime: Long = 0
    private val faceDetectionTolerance = 3000L

    private var fatigueListener: FatigueDetectionListener? = null

    // 記錄數據（供報告）
    private val earValues = mutableListOf<Float>()
    private val marValues = mutableListOf<Float>()
    private val fatigueEvents = mutableListOf<FatigueEvent>()

    fun processFaceLandmarks(result: FaceLandmarkerResult): FatigueDetectionResult {
        val now = System.currentTimeMillis()
        val hasFace = result.faceLandmarks().isNotEmpty()

        // 更新臉部狀態（不因短暫沒臉而 stopCalibration）
        updateFaceState(hasFace, now)

        if (!hasFace) {
            return FatigueDetectionResult(
                isFatigueDetected = false,
                fatigueLevel = FatigueLevel.NORMAL,
                events = emptyList(),
                faceDetected = false,
            )
        }

        val landmarks = result.faceLandmarks()[0]

        // 校正模式：累積樣本、回報進度；不做疲勞事件
        if (isCalibrating) {
            handleCalibration(landmarks, now)
            return FatigueDetectionResult(
                isFatigueDetected = false,
                fatigueLevel = FatigueLevel.NORMAL,
                events = emptyList(),
                faceDetected = true,
            )
        }

        val events = mutableListOf<FatigueEvent>()

        // 眼睛閉合
        detectEyeClosure(landmarks, now)?.let {
            events.add(it)
            fatigueEvents.add(it)
        }

        // 打哈欠 / 嘴巴張開
        detectYawn(landmarks, now)?.let {
            events.add(it)
            fatigueEvents.add(it)
        }

        // 高頻眨眼（每分鐘檢查一次）
        detectBlinkFrequency(landmarks, now)?.let {
            events.add(it)
            fatigueEvents.add(it)
        }

        // 更新累積規則 → 推導級別
        updateFatigueEventCount(events)

        val level = when {
            fatigueEventCount >= currentFatigueEventThreshold -> FatigueLevel.WARNING
            fatigueEventCount > 0 -> FatigueLevel.NOTICE
            else -> FatigueLevel.NORMAL
        }

        // 收集數據
        val ear = calculateCombinedEAR(landmarks)
        val mar = calculateMAR(landmarks, LandmarkIndices.MOUTH)
        earValues.add(ear); if (earValues.size > 500) earValues.removeAt(0)
        marValues.add(mar); if (marValues.size > 500) marValues.removeAt(0)

        return FatigueDetectionResult(
            isFatigueDetected = level != FatigueLevel.NORMAL,
            fatigueLevel = level,
            events = events,
            faceDetected = true,
        )
    }

    private fun updateFatigueEventCount(events: List<FatigueEvent>) {
        if (isCalibrating || !calibrationStateManager.hasCalibrated()) return

        fatigueEventCount = when {
            // 眼睛閉合超過門檻 → 直接警告
            events.any { it is FatigueEvent.EyeClosure } -> currentFatigueEventThreshold

            // 打哈欠：1 次提醒、2 次警告（這裡的 yawnCount 會被 detectYawn 更新）
            yawnCount >= 2 -> currentFatigueEventThreshold
            yawnCount >= 1 -> 1

            // 眨眼頻率：1 次提醒、2 次警告
            blinkFrequencyWarningCount >= 2 -> currentFatigueEventThreshold
            blinkFrequencyWarningCount >= 1 -> 1

            else -> 0
        }
    }

    private fun updateFaceState(hasFace: Boolean, now: Long) {
        if (hasFace) {
            isFaceDetected = true
            lastFaceDetectionTime = now
        } else {
            if (isFaceDetected && now - lastFaceDetectionTime > faceDetectionTolerance) {
                // 允許短暫沒臉；超過容忍才標記為沒臉
                isFaceDetected = false
                // 不在這裡 stopCalibration()，避免校正畫面突然消失
            }
        }
    }

    private fun detectEyeClosure(landmarks: List<NormalizedLandmark>, now: Long): FatigueEvent? {
        val ear = calculateCombinedEAR(landmarks)
        val eyesClosed = ear < currentEarThreshold

        return when {
            eyesClosed && !isEyeClosed -> {
                isEyeClosed = true
                lastEyeClosureStartTime = now
                null
            }
            eyesClosed && isEyeClosed -> {
                val dur = now - lastEyeClosureStartTime
                if (dur >= DEFAULT_EAR_CLOSURE_DURATION_THRESHOLD) {
                    // 超過門檻 → 警告事件。為了避免連續觸發，滑動起點
                    lastEyeClosureStartTime = now
                    FatigueEvent.EyeClosure(dur)
                } else null
            }
            !eyesClosed && isEyeClosed -> {
                val dur = now - lastEyeClosureStartTime
                isEyeClosed = false
                if (dur >= DEFAULT_EAR_CLOSURE_DURATION_THRESHOLD) {
                    FatigueEvent.EyeClosure(dur)
                } else {
                    detectBlink(now) // 正常眨眼
                    null
                }
            }
            else -> null
        }
    }

    /**
     * 同時負責：
     * - 長時間大張嘴 → 哈欠（Yawn）+ 張嘴次數
     * - 中等時間張嘴（>0.5s）→ 只算張嘴次數
     */
    private fun detectYawn(landmarks: List<NormalizedLandmark>, now: Long): FatigueEvent? {
        val mar = calculateMAR(landmarks, LandmarkIndices.MOUTH)
        val yawnMarThreshold = currentMarThreshold * 1.2f

        return when {
            mar > yawnMarThreshold && !isMouthOpen -> {
                // 嘴巴剛開始變大
                isMouthOpen = true
                lastMouthOpenStartTime = now
                null
            }
            mar > yawnMarThreshold && isMouthOpen -> {
                // 嘴巴持續張開中
                val openDur = now - lastMouthOpenStartTime
                if (openDur >= DEFAULT_YAWN_DURATION_THRESHOLD && mar > currentMarThreshold * 1.8f) {
                    // 明顯長時間大張嘴：當作一次「哈欠」＋「張嘴次數」
                    yawnCount++
                    mouthOpenCount++
                    mouthOpenTimestamps.add(now)
                    isMouthOpen = false
                    FatigueEvent.Yawn(openDur)
                } else null
            }
            mar <= yawnMarThreshold && isMouthOpen -> {
                // 嘴巴關起來了，來看這段算不算事件
                isMouthOpen = false
                val total = now - lastMouthOpenStartTime
                when {
                    // 原本就會算哈欠的條件
                    total >= DEFAULT_YAWN_DURATION_THRESHOLD -> {
                        yawnCount++
                        mouthOpenCount++
                        mouthOpenTimestamps.add(now)
                        FatigueEvent.Yawn(total)
                    }
                    total >= DEFAULT_YAWN_MIN_DURATION && mar > currentMarThreshold * 1.5f -> {
                        yawnCount++
                        mouthOpenCount++
                        mouthOpenTimestamps.add(now)
                        FatigueEvent.Yawn(total)
                    }
                    // 沒達到哈欠，但也張嘴超過 0.5 秒 → 純張嘴事件
                    total >= 500L -> {
                        mouthOpenCount++
                        mouthOpenTimestamps.add(now)
                        null
                    }
                    else -> null
                }
            }
            else -> null
        }
    }

    private fun detectBlinkFrequency(@Suppress("UNUSED_PARAMETER") landmarks: List<NormalizedLandmark>, now: Long): FatigueEvent? {
        if (now - lastMinuteStartTime >= 60000) {
            return if (blinkCount > DEFAULT_BLINK_FREQUENCY_THRESHOLD) {
                blinkFrequencyWarningCount++
                val evt = FatigueEvent.HighBlinkFrequency(blinkCount)
                blinkCount = 0
                lastMinuteStartTime = now
                evt
            } else {
                lastMinuteStartTime = now
                null
            }
        }
        return null
    }

    private fun detectBlink(now: Long) {
        val dt = now - lastBlinkTime
        if (dt > 200) {
            blinkCount++
            lastBlinkTime = now
            blinkTimestamps.add(now)
            fatigueListener?.onBlink()
        }
    }

    fun reset() {
        fatigueEventCount = 0
        blinkCount = 0
        yawnCount = 0
        blinkFrequencyWarningCount = 0
        isEyeClosed = false
        isMouthOpen = false
        lastEyeClosureStartTime = 0
        lastMouthOpenStartTime = 0
        lastBlinkTime = 0
        lastMinuteStartTime = System.currentTimeMillis()
        blinkTimestamps.clear()
        mouthOpenCount = 0
        mouthOpenTimestamps.clear()
        stopCalibration()
        isFaceDetected = false
    }

    fun resetFatigueEvents() {
        fatigueEventCount = 0
        blinkCount = 0
        yawnCount = 0
        blinkFrequencyWarningCount = 0
        isEyeClosed = false
        isMouthOpen = false
        lastEyeClosureStartTime = 0
        lastMouthOpenStartTime = 0
        lastBlinkTime = 0
        lastMinuteStartTime = System.currentTimeMillis()
        blinkTimestamps.clear()
        mouthOpenCount = 0
        mouthOpenTimestamps.clear()
    }

    private fun resetFatigueEventsAfterCalibration() {
        fatigueEventCount = 0
        blinkCount = 0
        yawnCount = 0
        blinkFrequencyWarningCount = 0
        isEyeClosed = false
        isMouthOpen = false
        lastEyeClosureStartTime = 0
        lastMouthOpenStartTime = 0
        lastBlinkTime = 0
        lastMinuteStartTime = System.currentTimeMillis()
        blinkTimestamps.clear()
        mouthOpenCount = 0
        mouthOpenTimestamps.clear()
    }

    fun setDetectionParameters(
        earThreshold: Float = currentEarThreshold,
        marThreshold: Float = currentMarThreshold,
        fatigueEventThreshold: Int = currentFatigueEventThreshold,
    ) {
        currentEarThreshold = earThreshold
        currentMarThreshold = marThreshold
        currentFatigueEventThreshold = fatigueEventThreshold
    }

    fun setFatigueListener(listener: FatigueDetectionListener) { this.fatigueListener = listener }

    fun getFatigueEventCount(): Int = fatigueEventCount

    fun getRecentBlinkCount(windowMs: Long): Int {
        val now = System.currentTimeMillis()
        blinkTimestamps.removeAll { now - it > windowMs }
        return blinkTimestamps.size
    }

    fun getYawnCount(): Int = yawnCount

    // 嘴巴張開次數
    fun getMouthOpenCount(): Int = mouthOpenCount

    fun getRecentMouthOpenCount(windowMs: Long = 60000L): Int {
        val now = System.currentTimeMillis()
        mouthOpenTimestamps.removeAll { now - it > windowMs }
        return mouthOpenTimestamps.size
    }

    fun getEyeClosureDuration(): Long {
        return if (isEyeClosed && lastEyeClosureStartTime > 0)
            System.currentTimeMillis() - lastEyeClosureStartTime
        else 0L
    }

    fun getRecentYawnCount(windowMs: Long = 60000L): Int = yawnCount

    fun startCalibration() {
        isCalibrating = true
        calibrationStartTime = System.currentTimeMillis()
        calibrationEarValues.clear()
        fatigueListener?.onCalibrationStarted()
    }

    fun stopCalibration() {
        isCalibrating = false
        calibrationEarValues.clear()
    }

    private fun handleCalibration(landmarks: List<NormalizedLandmark>, now: Long) {
        val elapsed = now - calibrationStartTime
        if (elapsed >= calibrationDuration) {
            finishCalibration()
            return
        }
        val ear = calculateCombinedEAR(landmarks)
        calibrationEarValues.add(ear)
        val progress = (elapsed * 100 / calibrationDuration).toInt()
        fatigueListener?.onCalibrationProgress(progress, ear)
    }

    private fun finishCalibration() {
        if (calibrationEarValues.isEmpty()) {
            stopCalibration()
            return
        }
        val sorted = calibrationEarValues.sorted()
        val minEar = sorted.first()
        val maxEar = sorted.last()
        val avgEar = calibrationEarValues.average().toFloat()
        val newThreshold = avgEar * 0.7f

        currentEarThreshold = newThreshold
        calibrationStateManager.markCalibrationCompleted()
        resetFatigueEventsAfterCalibration()

        fatigueListener?.onCalibrationCompleted(newThreshold, minEar, maxEar, avgEar)
        stopCalibration()
    }

    fun isCalibrating(): Boolean = isCalibrating
    fun resetCalibrationState() { calibrationStateManager.resetCalibrationState() }

    fun getResetStatusInfo(): String =
        "ResetProtection: false, Cooldown: false, FatigueCount: $fatigueEventCount"

    fun getCalibrationProgress(): Int {
        if (!isCalibrating) return 0
        val elapsed = System.currentTimeMillis() - calibrationStartTime
        return (elapsed * 100 / calibrationDuration).toInt().coerceIn(0, 100)
    }

    fun isFaceDetected(): Boolean = isFaceDetected

    fun generateSensitivityReport(): String {
        val thresholds = mapOf(
            "ear" to currentEarThreshold,
            "mar" to currentMarThreshold,
            "fatigueEvent" to currentFatigueEventThreshold.toFloat(),
        )
        val eventCounts = fatigueEvents.groupBy { it.javaClass.simpleName }.mapValues { it.value.size }
        return FatigueDetectionLogger.generateAnalysisReport(
            earValues = earValues.toList(),
            marValues = marValues.toList(),
            eventCounts = eventCounts,
            calibrationData = thresholds,
        )
    }

    fun getDetectionParameters(): Map<String, Any> = mapOf(
        "earThreshold" to currentEarThreshold,
        "marThreshold" to currentMarThreshold,
        "fatigueEventThreshold" to currentFatigueEventThreshold,
        "earClosureDurationThreshold" to DEFAULT_EAR_CLOSURE_DURATION_THRESHOLD,
        "yawnDurationThreshold" to DEFAULT_YAWN_DURATION_THRESHOLD,
        "yawnMinDuration" to DEFAULT_YAWN_MIN_DURATION,
        "blinkFrequencyThreshold" to DEFAULT_BLINK_FREQUENCY_THRESHOLD,
        "calibrationDuration" to calibrationDuration,
        "hasCalibrated" to calibrationStateManager.hasCalibrated(),
        "isCalibrating" to isCalibrating,
        "fatigueEventCount" to fatigueEventCount,
        "blinkCount" to blinkCount,
        "yawnCount" to yawnCount,
        "blinkFrequencyWarningCount" to blinkFrequencyWarningCount,
    )

    fun setLogEnabled(
        sensitivity: Boolean = true,
        trigger: Boolean = true,
        calibration: Boolean = true,
        event: Boolean = true,
        reset: Boolean = true,
    ) {
        FatigueDetectionLogger.setLogEnabled(sensitivity, trigger, calibration, event, reset)
    }

    // ===== EAR / MAR =====
    private fun calculateEAR(landmarks: List<NormalizedLandmark>, idx: List<Int>): Float {
        if (idx.size < 6) return 0f
        val p1 = landmarks[idx[0]]; val p2 = landmarks[idx[1]]; val p3 = landmarks[idx[2]]
        val p4 = landmarks[idx[3]]; val p5 = landmarks[idx[4]]; val p6 = landmarks[idx[5]]
        val A = euclideanDistance(p2, p6); val B = euclideanDistance(p3, p5); val C = euclideanDistance(p1, p4)
        return (A + B) / (2.0f * C)
    }

    private fun calculateCombinedEAR(landmarks: List<NormalizedLandmark>): Float {
        val left = calculateEAR(landmarks, LandmarkIndices.LEFT_EYE)
        val right = calculateEAR(landmarks, LandmarkIndices.RIGHT_EYE)
        return (left + right) / 2.0f
    }

    private fun calculateMAR(landmarks: List<NormalizedLandmark>, mouth: List<Int>): Float {
        if (mouth.size < 6) return 0f
        val p1 = landmarks[mouth[0]]; val p2 = landmarks[mouth[1]]; val p3 = landmarks[mouth[2]]
        val p4 = landmarks[mouth[3]]; val p5 = landmarks[mouth[4]]; val p6 = landmarks[mouth[5]]
        val A = euclideanDistance(p2, p6); val B = euclideanDistance(p3, p5); val C = euclideanDistance(p1, p4)
        return (A + B) / (2.0f * C)
    }

    private fun euclideanDistance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        return sqrt(dx * dx + dy * dy)
    }

    // overload for float coords（現在沒用到也沒關係，保留）
    private fun euclideanDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }
}
