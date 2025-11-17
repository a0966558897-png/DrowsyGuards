package com.patrick.ui.fatigue

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.patrick.core.FatigueDetectionDebugger
import com.patrick.core.FatigueLevel
import com.patrick.core.FatigueUiCallback
import com.patrick.detection.FatigueDetectionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.net.Uri
import android.content.Intent


class FatigueViewModel(
    application: Application,
) : AndroidViewModel(application), FatigueUiCallback {

    companion object {
        private const val TAG = "FatigueViewModel"
        private const val AUTO_ENABLE_DEBUG = false

        // —— 調整這兩個就能控制下降速度 ——
        const val RED_THRESHOLD = 70           // 紅色門檻
        const val REARM_GAP = 5                // 重新武裝間隙（避免一直彈）
        const val DECAY_STEP = 1               // 每次遞減「1 分」（原 2）
        const val DECAY_PERIOD_MS = 2500L      // 每 1.5 秒遞減一次（原 1000ms）
    }

    sealed class FatigueUiEvent {
        object ShowWarningDialog : FatigueUiEvent()
        object ShowNoticeDialog  : FatigueUiEvent()
    }
    private val _uiEvent = MutableSharedFlow<FatigueUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val fatigueDetectionManager = FatigueDetectionManager(application, this)
    private val fatigueUiStateManager   = FatigueUiStateManager()
    private val debugger                = FatigueDetectionDebugger(application)

    private val _fatigueLevel = MutableStateFlow(FatigueLevel.NORMAL)
    val fatigueLevel: StateFlow<FatigueLevel> = _fatigueLevel

    private val _fatigueScore = MutableStateFlow(0)
    val fatigueScore: StateFlow<Int> = _fatigueScore

    private val _showFatigueDialog = MutableStateFlow(false)
    val showFatigueDialog: StateFlow<Boolean> = _showFatigueDialog

    private val _statusText = MutableStateFlow("持續偵測中…")
    val statusText: StateFlow<String> = _statusText

    private val _isFaceDetected = MutableStateFlow(false)
    val isFaceDetected: StateFlow<Boolean> = _isFaceDetected

    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating

    private val _calibrationProgress = MutableStateFlow(0)
    val calibrationProgress: StateFlow<Int> = _calibrationProgress

    private val _calibrationEarValue = MutableStateFlow(0f)
    val calibrationEarValue: StateFlow<Float> = _calibrationEarValue

    private val _blinkFrequency = MutableStateFlow(0)
    val blinkFrequency: StateFlow<Int> = _blinkFrequency

    private val _showBlinkFrequency = MutableStateFlow(true)
    val showBlinkFrequency: StateFlow<Boolean> = _showBlinkFrequency

    private val _yawnCount = MutableStateFlow(0)
    val yawnCount: StateFlow<Int> = _yawnCount

    private val _eyeClosureDuration = MutableStateFlow(0L)
    val eyeClosureDuration: StateFlow<Long> = _eyeClosureDuration

    /** 對話框顯示期間「鎖分數不下降」 */
    private var pauseScoreDecay: Boolean = false

    init {
        fatigueDetectionManager.setProcessingRateFps(12)
        if (AUTO_ENABLE_DEBUG) debugger.enableQuickDebugMode()

        // 背景遞減協程（放慢版）
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                kotlinx.coroutines.delay(DECAY_PERIOD_MS)
                if (!pauseScoreDecay && _fatigueScore.value > 0) {
                    _fatigueScore.value = (_fatigueScore.value - DECAY_STEP).coerceAtLeast(0)
                    // 低於 rearm 線，允許下一次再跳警告
                    if (_fatigueScore.value <= RED_THRESHOLD - REARM_GAP) {
                        fatigueUiStateManager.setWarningDialogActive(false)
                    }
                }
            }
        }
    }

    // 外部 API
    fun processFaceLandmarks(result: FaceLandmarkerResult) =
        fatigueDetectionManager.processFaceLandmarks(result)

    fun startDetection() {
        fatigueDetectionManager.startDetection()
        Log.d(TAG, "疲勞檢測已啟動")
    }

    fun stopDetection() {
        fatigueDetectionManager.stopDetection()
        Log.d(TAG, "疲勞檢測已停止")
    }

    fun startCalibration() {
        _isCalibrating.value = true
        _calibrationProgress.value = 0
        _statusText.value = "校正中，請自然眨眼 15 秒…"
        fatigueDetectionManager.startCalibration()
    }

    fun stopCalibration() {
        _isCalibrating.value = false
        fatigueDetectionManager.stopCalibration()
    }

    /** 我已清醒：關窗→恢復分數更新（才會開始往下降） */
    fun handleUserAcknowledged() {
        fatigueUiStateManager.onUserAcknowledged()
        try { fatigueDetectionManager.acknowledgeWarning() } catch (_: Throwable) {}
        _showFatigueDialog.value = false
        _statusText.value = "持續偵測中…"
        fatigueUiStateManager.setWarningDialogActive(false)
        pauseScoreDecay = false
    }

    /** 我會休息：關窗→可清事件→恢復分數更新 */
    fun handleUserRequestedRest() {
        fatigueUiStateManager.onUserRequestedRest()
        _showFatigueDialog.value = false
        _statusText.value = "持續偵測中…"
        fatigueDetectionManager.resetFatigueEvents()
        fatigueUiStateManager.setWarningDialogActive(false)
        pauseScoreDecay = false

        // ⭐⭐ 正確的地圖跳轉（無紅線版）⭐⭐
        try {
            val ctx = getApplication<Application>().applicationContext

            // ---------- 1. 先試 Google Maps ----------
            val gmapUri = Uri.parse("geo:0,0?q=休息站")
            val gmapIntent = Intent(Intent.ACTION_VIEW, gmapUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (gmapIntent.resolveActivity(ctx.packageManager) != null) {
                Log.e("FATIGUE_REST", "✔ 使用 Google Maps 開啟休息站")
                ctx.startActivity(gmapIntent)
                return
            }

            // ---------- 2. 再試一般地圖 App ----------
            val anyMapIntent = Intent(Intent.ACTION_VIEW, gmapUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (anyMapIntent.resolveActivity(ctx.packageManager) != null) {
                Log.e("FATIGUE_REST", "✔ 使用其他地圖 App 開啟休息站")
                ctx.startActivity(anyMapIntent)
                return
            }

            // ---------- 3. 最後用瀏覽器 ----------
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/休息站/")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            Log.e("FATIGUE_REST", "✔ 使用瀏覽器開啟休息站")
            ctx.startActivity(webIntent)

        } catch (e: Exception) {
            Log.e("FATIGUE_REST", "❌ 地圖跳轉失敗: ${e.message}")
        }
    }


    fun generateDebugReport(): String = try {
        debugger.generateDebugReport(
            parameters = fatigueDetectionManager.getDetectionParameters(),
            sensitivityReport = fatigueDetectionManager.generateSensitivityReport(),
            uiStateInfo = fatigueUiStateManager.getResetStatusInfo(),
        )
    } catch (e: Exception) { "生成調試報告失敗: ${e.message}" }

    fun saveDebugReport(): String = try {
        debugger.saveDebugReport(generateDebugReport())
    } catch (e: Exception) { "保存失敗: ${e.message}" }

    fun setDebugMode(mode: String) {
        when (mode.lowercase()) {
            "quick"       -> debugger.enableQuickDebugMode()
            "sensitivity" -> debugger.enableSensitivityDebugMode()
            "performance" -> debugger.enablePerformanceDebugMode()
            "off"         -> debugger.disableAllLogs()
            else          -> Log.w(TAG, "未知的調試模式: $mode")
        }
    }

    fun setMinProcessIntervalMs(intervalMs: Long) =
        fatigueDetectionManager.setMinProcessIntervalMs(intervalMs)

    fun setProcessingRateFps(fps: Int) =
        fatigueDetectionManager.setProcessingRateFps(fps)

    fun checkAutoReport(): String? = try {
        debugger.checkAutoGenerateReport(
            parameters = fatigueDetectionManager.getDetectionParameters(),
            sensitivityReport = fatigueDetectionManager.generateSensitivityReport(),
            uiStateInfo = fatigueUiStateManager.getResetStatusInfo(),
        )
    } catch (_: Exception) { null }

    // ====== FatigueUiCallback ======
    override fun onBlink() {}

    override fun onCalibrationStarted() {
        _isCalibrating.value = true
        _calibrationProgress.value = 0
        _statusText.value = "正在校正中..."
    }

    override fun onCalibrationProgress(progress: Int, currentEar: Float) {
        _calibrationProgress.value = progress
        _statusText.value = "正在校正中... $progress%"
    }

    override fun onCalibrationCompleted(newThreshold: Float, minEar: Float, maxEar: Float, avgEar: Float) {
        _isCalibrating.value = false
        _calibrationProgress.value = 100
        _calibrationEarValue.value = avgEar
        _statusText.value = "校正完成！EAR: ${String.format("%.3f", avgEar)}, 閾值: ${String.format("%.3f", newThreshold)}"
    }

    override fun onNoticeFatigue() {
        // 對話窗期間不覆蓋 WARNING 狀態
        if (fatigueUiStateManager.hasActiveWarningDialog() || _showFatigueDialog.value) return
        val processed = fatigueUiStateManager.processFatigueResult(
            FatigueLevel.NOTICE, fatigueDetectionManager.getFatigueEventCount()
        )
        updateUIState(processed, false, generateStatusMessage(processed, true))
        CoroutineScope(Dispatchers.Main).launch { _uiEvent.emit(FatigueUiEvent.ShowNoticeDialog) }
    }

    override fun onNormalDetection() {
        // 對話窗期間不覆蓋 WARNING 狀態
        if (fatigueUiStateManager.hasActiveWarningDialog() || _showFatigueDialog.value) return
        val processed = fatigueUiStateManager.processFatigueResult(
            FatigueLevel.NORMAL, fatigueDetectionManager.getFatigueEventCount()
        )
        updateUIState(processed, false, generateStatusMessage(processed, true))
    }

    override fun onNoFaceDetected() {
        // 校正期間不覆蓋 UI（避免校正中畫面消失）
        if (_isCalibrating.value) return

        val hasActiveDialog = _showFatigueDialog.value || fatigueUiStateManager.hasActiveWarningDialog()
        if (!hasActiveDialog) {
            updateUIState(FatigueLevel.NORMAL, false, "請面對鏡頭", faceDetected = false)
        } else {
            _isFaceDetected.value = false
        }
    }

    override fun onWarningFatigue() {
        // WARNING 一律打開對話框，並鎖定分數不下降
        _showFatigueDialog.value = true
        pauseScoreDecay = true

        val processed = fatigueUiStateManager.processFatigueResult(
            FatigueLevel.WARNING, fatigueDetectionManager.getFatigueEventCount()
        )
        updateUIState(processed, true, generateStatusMessage(processed, true))
        fatigueUiStateManager.setWarningDialogActive(true)
        CoroutineScope(Dispatchers.Main).launch { _uiEvent.emit(FatigueUiEvent.ShowWarningDialog) }
    }

    // 來自 AlertManager 的回呼（保險：同樣只關窗＋回偵測中文案，並解除鎖定）
    override fun onUserAcknowledged() {
        fatigueUiStateManager.onUserAcknowledged()
        _showFatigueDialog.value = false
        _statusText.value = "持續偵測中…"
        fatigueUiStateManager.setWarningDialogActive(false)
        pauseScoreDecay = false
    }

    override fun onUserRequestedRest() {
        fatigueUiStateManager.onUserRequestedRest()
        _showFatigueDialog.value = false
        _statusText.value = "持續偵測中…"
        fatigueUiStateManager.setWarningDialogActive(false)
        pauseScoreDecay = false
    }

    override fun setWarningDialogActive(active: Boolean) { /* no-op */ }

    override fun onFatigueScoreUpdated(score: Int, level: FatigueLevel) {
        // 分數：對話窗期間只允許「上升」，不允許「下降」
        if (pauseScoreDecay) {
            _fatigueScore.value = maxOf(_fatigueScore.value, score)
            if (_fatigueLevel.value != FatigueLevel.WARNING) {
                _fatigueLevel.value = FatigueLevel.WARNING
            }
            return
        }
        // 正常狀態：照常更新
        _fatigueScore.value = score
        if (_fatigueLevel.value != level) _fatigueLevel.value = level

        if (!_isCalibrating.value) {
            _statusText.value = generateStatusMessage(_fatigueLevel.value, _isFaceDetected.value)
        }
    }

    // ===== 私有輔助 =====
    private fun updateUIState(
        level: FatigueLevel,
        showDialog: Boolean,
        status: String,
        faceDetected: Boolean = true,
    ) {
        _fatigueLevel.value = if (_showFatigueDialog.value || pauseScoreDecay) {
            FatigueLevel.WARNING
        } else level

        _showFatigueDialog.value = showDialog
        _statusText.value = status
        _isFaceDetected.value = faceDetected
        updateDetectionData()
    }

    private fun updateDetectionData() {
        try {
            _yawnCount.value = fatigueDetectionManager.getYawnCount()
            _eyeClosureDuration.value = fatigueDetectionManager.getEyeClosureDuration()
            _blinkFrequency.value = fatigueDetectionManager.getRecentBlinkCount(60000L)
        } catch (e: Exception) {
            Log.e(TAG, "更新偵測數據失敗", e)
        }
    }

    private fun generateStatusMessage(
        fatigueLevel: FatigueLevel,
        faceDetected: Boolean,
        isCalibrating: Boolean = _isCalibrating.value
    ): String = when {
        isCalibrating -> "正在校正中..."
        !faceDetected -> "請面對鏡頭"
        fatigueLevel == FatigueLevel.WARNING -> "⚠️ 疲勞警告"
        fatigueLevel == FatigueLevel.NOTICE  -> "⚠️ 疲勞提醒"
        else -> "持續偵測中…"
    }

    fun getResetStatusInfo(): String = fatigueUiStateManager.getResetStatusInfo()
}
