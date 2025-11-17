package com.patrick.detection

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.components.containers.Classifications
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.patrick.alert.FatigueAlertManager
import com.patrick.core.FatigueDialogCallback
import com.patrick.core.FatigueDetectionListener
import com.patrick.core.FatigueDetectionResult
import com.patrick.core.FatigueEvent
import com.patrick.core.FatigueLevel
import com.patrick.core.FatigueUiCallback
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import android.content.Intent
import android.net.Uri


/**
 * 疲勞檢測管理器
 * 協調疲勞檢測器、警報管理器和 UI 回調
 */
class FatigueDetectionManager(
    private val context: Context,
    private var uiCallback: FatigueUiCallback? = null,
) : FatigueDetectionListener {

    companion object {
        private const val TAG = "FatigueDetectionManager"
        private const val NO_FACE_FRAME_THRESHOLD = 5
        private const val COOLDOWN_MS = 8000L // 使用者按「我已清醒」後的短暫冷卻
    }

    private val fatigueDetector = FatigueDetector(context)
    private val alertManager = FatigueAlertManager(context)

    // —— 覆層：Blendshapes 打呵欠/張嘴偵測 ——
    private val yawnDetector = YawnDetector()
    private var overlayYawnCount = 0
    private val overlayYawnTimestamps = CopyOnWriteArrayList<Long>()
    private var lastYawnTriggeredFlag = false // 本幀是否偵測到覆層 yawn

    // 簡易狀態機
    enum class DetectionState { INITIALIZING, CALIBRATING, DETECTING, NOTICE, WARNING, NO_FACE, REST_MODE, ERROR, SHUTDOWN }

    private var lastKnownState: DetectionState = DetectionState.DETECTING
    private var currentState: DetectionState = DetectionState.INITIALIZING
        set(value) {
            if (field != value) {
                handleStateExit(field)
                field = value
                handleStateEnter(value)
            }
        }

    private var lastError: Exception? = null
    private var noFaceFrameCount = 0
    private var minProcessIntervalMs: Long = 50L
    private var lastProcessedTimestamp: Long = 0L

    // 冷卻：使用者按「我已清醒」後一段時間內不再彈窗，並加速分數回復
    private var cooldownUntil: Long = 0L
    private fun inCooldown(): Boolean = System.currentTimeMillis() < cooldownUntil

    // ====== 分數引擎（時間制恢復 + 事件懲罰 + 短暫鎖恢復）======
    private object FatigueScoreEngine {
        private const val YAWN_PENALTY = 10      // 👈 張嘴/打呵欠一次 +10 分
        private const val BLINK_PENALTY = 10

        // 回復參數
        private const val RECOVER_STEP = 1
        private const val RECOVER_PERIOD_MS = 1500L
        private const val FAST_RECOVER_STEP = 3
        private const val FAST_RECOVER_PERIOD_MS = 1000L

        private const val HOLD_AFTER_YAWN_MS = 2000L

        // 長閉眼時要拉到的目標分數（你設定的 70）
        private const val EYE_CLOSURE_FORCE_SCORE = 70

        private var score = 0
        private var lastRecoverAt: Long = 0L
        private var holdUntil: Long = 0L

        fun reset() {
            score = 0
            lastRecoverAt = 0L
            holdUntil = 0L
        }

        fun getScore(): Int = score

        fun getLevel(): FatigueLevel = when {
            score >= 61 -> FatigueLevel.WARNING
            score >= 31 -> FatigueLevel.NOTICE
            else        -> FatigueLevel.NORMAL
        }

        fun addYawnPenalty(now: Long) {
            score = (score + YAWN_PENALTY).coerceAtMost(100)
            // 張嘴/打呵欠後，至少這段時間不恢復
            holdUntil = kotlin.math.max(holdUntil, now + HOLD_AFTER_YAWN_MS)
        }

        fun addBlinkPenalty() {
            score = (score + BLINK_PENALTY).coerceAtMost(100)
        }

        // 閉眼 ≥ 門檻 → 分數至少拉到 70（不覆蓋更高分）
        fun addEyeClosurePenalty() {
            score = kotlin.math.max(score, EYE_CLOSURE_FORCE_SCORE)
        }

        fun recover(now: Long, fast: Boolean) {
            if (now < holdUntil) return

            val (step, period) = if (fast) {
                FAST_RECOVER_STEP to FAST_RECOVER_PERIOD_MS
            } else {
                RECOVER_STEP to RECOVER_PERIOD_MS
            }

            if (lastRecoverAt == 0L) {
                lastRecoverAt = now
                return
            }

            if (now - lastRecoverAt >= period && score > 0) {
                score = (score - step).coerceAtLeast(0)
                lastRecoverAt = now
            }
        }
    }
    private fun openNearestRestStop(context: Context) {
        Log.e("FATIGUE_REST", "🧭 openNearestRestStop() 被呼叫")

        try {
            val uri = Uri.parse("geo:0,0?q=休息站")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addCategory(Intent.CATEGORY_DEFAULT)
            }

            // 優先 Google Maps
            val gmapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val pm = context.packageManager

            // 如果有 Google Maps
            if (gmapIntent.resolveActivity(pm) != null) {
                Log.e("FATIGUE_REST", "✔ 開啟 Google Maps")
                context.startActivity(gmapIntent)
                return
            }

            // 其他地圖
            if (intent.resolveActivity(pm) != null) {
                Log.e("FATIGUE_REST", "✔ 開啟一般地圖 APP")
                context.startActivity(intent)
                return
            }

            // 最後 fallback → 用 Chrome 開 Google 地圖搜尋
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/休息站/")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            Log.e("FATIGUE_REST", "✔ Fallback → 用瀏覽器開 Google Maps")
            context.startActivity(webIntent)

        } catch (e: Exception) {
            Log.e("FATIGUE_REST", "❌ 開啟地圖失敗: ${e.message}", e)
        }
    }






    private fun handleStateExit(state: DetectionState) {
        if (state == DetectionState.WARNING) {
            uiCallback?.setWarningDialogActive(false)
        }
    }

    private fun handleStateEnter(state: DetectionState) {
        when (state) {
            DetectionState.NO_FACE -> {
                uiCallback?.onNoFaceDetected()
                alertManager.stopAllAlerts()
            }
            DetectionState.WARNING -> {
                uiCallback?.onWarningFatigue()
                uiCallback?.setWarningDialogActive(true)
            }
            DetectionState.NOTICE -> uiCallback?.onNoticeFatigue()
            DetectionState.ERROR -> {
                alertManager.stopAllAlerts()
                uiCallback?.setWarningDialogActive(false)
            }
            DetectionState.CALIBRATING -> {
                uiCallback?.onCalibrationStarted()
                alertManager.stopAllAlerts()
            }
            DetectionState.DETECTING -> uiCallback?.onNormalDetection()
            DetectionState.REST_MODE -> {
                alertManager.stopAllAlerts()
                uiCallback?.setWarningDialogActive(false)
            }
            DetectionState.SHUTDOWN -> {
                alertManager.stopAllAlerts()
                uiCallback?.setWarningDialogActive(false)
            }
            else -> {}
        }
    }

    init {
        fatigueDetector.setFatigueListener(this)
        alertManager.setDialogCallback(object : FatigueDialogCallback {
            override fun onUserAcknowledged() {
                acknowledgeWarning()
                uiCallback?.onUserAcknowledged()
                transitionToState(DetectionState.DETECTING)
            }
            override fun onUserRequestedRest() {
                alertManager.stopAllAlerts()
                uiCallback?.onUserRequestedRest()

                // ⭐ 自動跳到 Google Maps → 最近的休息站
                openNearestRestStop(context)

                transitionToState(DetectionState.REST_MODE)
            }

        })
        uiCallback?.let { alertManager.setUiCallback(it) }
    }

    fun setMinProcessIntervalMs(intervalMs: Long) { minProcessIntervalMs = intervalMs }
    fun setProcessingRateFps(fps: Int) { minProcessIntervalMs = (1000L / fps.coerceIn(1, 60)) }

    fun acknowledgeWarning() {
        cooldownUntil = System.currentTimeMillis() + COOLDOWN_MS
        uiCallback?.setWarningDialogActive(false)
        uiCallback?.onNormalDetection()
        transitionToState(DetectionState.DETECTING)
    }

    fun processFaceLandmarks(result: FaceLandmarkerResult) {
        if (currentState == DetectionState.SHUTDOWN || currentState == DetectionState.ERROR) return

        val now = System.currentTimeMillis()
        val elapsed = now - lastProcessedTimestamp
        if (elapsed in 0 until minProcessIntervalMs) return
        lastProcessedTimestamp = now

        try {
            lastYawnTriggeredFlag = false

            val fatigueResult = fatigueDetector.processFaceLandmarks(result)

            // ===== 校正期間：完全不轉 NO_FACE，也不跑告警/分數 =====
            if (currentState == DetectionState.CALIBRATING) return

            // 覆層：blendshapes 嘴巴張開偵測（jawOpen / mouthFunnel）
            try {
                val openScore = extractMouthOpenScore(result)   // 相容層
                if (openScore != null) {
                    // 🔴 關鍵：這裡一定要傳 now（tsMs）
                    val yd = yawnDetector.update(openScore, now)
                    if (yd.yawnTriggered) {
                        lastYawnTriggeredFlag = true
                        overlayYawnCount += 1
                        overlayYawnTimestamps += now
                        Log.d(
                            TAG,
                            "yawn(triggered): ema=%.2f base=%.2f th=%.2f"
                                .format(yd.scoreEma, yd.baseline, yd.threshold)
                        )
                    }
                }
            } catch (_: Throwable) { /* 單幀異常忽略 */ }

            if (fatigueResult.faceDetected) {
                noFaceFrameCount = 0
                if (currentState == DetectionState.NO_FACE) transitionToState(lastKnownState)
                if (currentState != DetectionState.NO_FACE) {
                    handleAlerts(fatigueResult, now)
                }
            } else {
                if (++noFaceFrameCount >= NO_FACE_FRAME_THRESHOLD && currentState != DetectionState.NO_FACE) {
                    lastKnownState = currentState
                    transitionToState(DetectionState.NO_FACE)
                    noFaceFrameCount = 0
                }
            }
        } catch (e: Exception) {
            lastError = e
            transitionToState(DetectionState.ERROR)
        }
    }

    private fun handleAlerts(result: FatigueDetectionResult, now: Long) {
        if (currentState in listOf(
                DetectionState.CALIBRATING,
                DetectionState.NO_FACE,
                DetectionState.ERROR,
                DetectionState.SHUTDOWN
            )
        ) return

        // —— 事件 → 懲罰；否則 → 按時間恢復 ——
        when {
            // ⬇ 閉眼 ≥ 1 秒：看 FatigueEvent.EyeClosure
            result.events.any { it is FatigueEvent.EyeClosure } -> {
                FatigueScoreEngine.addEyeClosurePenalty()
            }

            // 張嘴 / 打呵欠（底層事件 or overlay 事件）：+10 分
            lastYawnTriggeredFlag || result.events.any { it is FatigueEvent.Yawn } -> {
                FatigueScoreEngine.addYawnPenalty(now)
            }

            // 一分鐘內眨眼太多：+10 分
            getRecentBlinkCount(60000L) > 25 -> {
                FatigueScoreEngine.addBlinkPenalty()
            }

            else -> {
                // 沒有新的事件，照時間慢慢恢復
                FatigueScoreEngine.recover(now, fast = inCooldown())
            }
        }
        lastYawnTriggeredFlag = false

        val score = FatigueScoreEngine.getScore()
        val level = FatigueScoreEngine.getLevel()
        uiCallback?.onFatigueScoreUpdated(score, level)

        // 冷卻期間不彈窗，維持偵測狀態就好
        if (inCooldown()) {
            transitionToState(DetectionState.DETECTING)
            return
        }

        if (result.isFatigueDetected) {
            alertManager.handleFatigueDetection(result)
            when (result.fatigueLevel) {
                FatigueLevel.NOTICE  -> transitionToState(DetectionState.NOTICE)
                FatigueLevel.WARNING -> transitionToState(DetectionState.WARNING)
                else                 -> transitionToState(DetectionState.DETECTING)
            }
        } else {
            transitionToState(DetectionState.DETECTING)
        }
    }

    private fun transitionToState(newState: DetectionState) {
        val previous = currentState
        if (newState == DetectionState.NO_FACE && previous != DetectionState.NO_FACE) {
            lastKnownState = previous
        }
        currentState = newState
        Log.d(TAG, "Transitioned to state: $previous -> $newState")
        handleStateEnter(newState)
    }

    // 外部控制
    fun startDetection() {
        resetFatigueEvents()
        FatigueScoreEngine.reset()
        cooldownUntil = 0L
        overlayYawnCount = 0
        overlayYawnTimestamps.clear()
        yawnDetector.reset()
        transitionToState(DetectionState.DETECTING)
    }

    fun stopDetection() {
        fatigueDetector.reset()
        alertManager.stopAllAlerts()
        transitionToState(DetectionState.SHUTDOWN)
    }

    fun startCalibration() {
        fatigueDetector.startCalibration()
        transitionToState(DetectionState.CALIBRATING)
    }

    fun stopCalibration() {
        fatigueDetector.stopCalibration()
        transitionToState(DetectionState.DETECTING)
    }

    // 代理一些查詢/設定
    fun setDetectionParameters(earThreshold: Float, marThreshold: Float, fatigueEventThreshold: Int) {
        fatigueDetector.setDetectionParameters(earThreshold, marThreshold, fatigueEventThreshold)
    }

    fun getFatigueEventCount(): Int = fatigueDetector.getFatigueEventCount()
    fun resetFatigueEvents() { fatigueDetector.resetFatigueEvents() }

    fun fullResetDetectorAndAlerts() {
        resetFatigueEvents()
        fatigueDetector.reset()
        FatigueScoreEngine.reset()
        alertManager.stopAllAlerts()
        uiCallback?.setWarningDialogActive(false)
        cooldownUntil = 0L
        overlayYawnCount = 0
        overlayYawnTimestamps.clear()
        yawnDetector.reset()
        transitionToState(DetectionState.DETECTING)
    }

    fun getDetectionParameters(): Map<String, Any> = fatigueDetector.getDetectionParameters()
    fun generateSensitivityReport(): String = fatigueDetector.generateSensitivityReport()

    fun setLogEnabled(
        sensitivity: Boolean = true,
        trigger: Boolean = true,
        calibration: Boolean = true,
        event: Boolean = true,
        reset: Boolean = true,
    ) {
        fatigueDetector.setLogEnabled(sensitivity, trigger, calibration, event, reset)
    }

    fun getRecentBlinkCount(windowMs: Long): Int = fatigueDetector.getRecentBlinkCount(windowMs)

    // 覆寫：回傳（底層 + 覆層）打呵欠/張嘴 次數
    fun getYawnCount(): Int = fatigueDetector.getYawnCount() + overlayYawnCount

    // 覆寫：回傳（底層 + 覆層）近窗打呵欠/張嘴 次數
    fun getRecentYawnCount(windowMs: Long = 60000L): Int {
        val cutoff = System.currentTimeMillis() - windowMs
        overlayYawnTimestamps.removeIf { it < cutoff } // 清舊
        return fatigueDetector.getRecentYawnCount(windowMs) + overlayYawnTimestamps.count { it >= cutoff }
    }

    // 嘴巴張開次數（底層統計）
    fun getMouthOpenCount(): Int = fatigueDetector.getMouthOpenCount()

    fun getRecentMouthOpenCount(windowMs: Long = 60000L): Int =
        fatigueDetector.getRecentMouthOpenCount(windowMs)

    fun getEyeClosureDuration(): Long = fatigueDetector.getEyeClosureDuration()
    fun isCalibrating(): Boolean = fatigueDetector.isCalibrating()
    fun getCalibrationProgress(): Int = fatigueDetector.getCalibrationProgress()
    fun isFaceDetected(): Boolean = fatigueDetector.isFaceDetected()

    // Listener 轉拋
    override fun onCalibrationStarted() { transitionToState(DetectionState.CALIBRATING) }
    override fun onCalibrationProgress(progress: Int, currentEar: Float) {
        uiCallback?.onCalibrationProgress(progress, currentEar)
    }
    override fun onCalibrationCompleted(newThreshold: Float, minEar: Float, maxEar: Float, avgEar: Float) {
        transitionToState(DetectionState.DETECTING)
        uiCallback?.onCalibrationCompleted(newThreshold, minEar, maxEar, avgEar)
    }
    override fun onFatigueDetected(result: FatigueDetectionResult) {}
    override fun onFatigueLevelChanged(level: FatigueLevel) {}
    override fun onBlink() { uiCallback?.onBlink() }

    // —— 相容層：不同 MediaPipe 版本的 blendshapes 取法 ——
    private fun getBlendshapesCompat(result: FaceLandmarkerResult): List<Classifications>? {
        try {
            val m = result::class.java.getMethod("faceBlendshapes")
            @Suppress("UNCHECKED_CAST")
            return m.invoke(result) as? List<Classifications>
        } catch (_: Throwable) {}
        return try {
            val m2 = result::class.java.getMethod("getFaceBlendshapes")
            @Suppress("UNCHECKED_CAST")
            m2.invoke(result) as? List<Classifications>
        } catch (_: Throwable) { null }
    }

    // 從 result 擷取嘴巴張開分數：max(jawOpen, mouthFunnel)
// ======== 嘴巴張開（MAR）偵測 ========
    private fun extractMouthOpenScore(result: FaceLandmarkerResult): Float? {
        val landmarks = result.faceLandmarks() ?: return null
        if (landmarks.isEmpty()) return null

        val lm = landmarks[0]  // 第一張臉

        // 確保 landmark 點數夠
        if (lm.size <= 308) return null

        // 嘴巴 MediaPipe 固定 index
        val topLip = lm[13]
        val bottomLip = lm[14]
        val leftLip = lm[78]
        val rightLip = lm[308]

        // 計算 MAR
        val vertical = distance(topLip.x(), topLip.y(), bottomLip.x(), bottomLip.y())
        val horizontal = distance(leftLip.x(), leftLip.y(), rightLip.x(), rightLip.y())

        if (horizontal == 0f) return null

        val mar = vertical / horizontal

        // 直接回傳 raw MAR（最準）
        return mar
    }

    // 工具函式
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }


}
