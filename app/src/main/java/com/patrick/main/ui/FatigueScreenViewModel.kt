package com.patrick.main.ui

import android.app.Application
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.patrick.camera.CameraViewModel
import com.patrick.ui.fatigue.FatigueViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * UI 協調 ViewModel：橋接 Camera 與 FatigueViewModel
 * - 負責相機初始化/釋放，並把 landmarks 傳給疲勞檢測
 * - 偵測流程 start/stop 由外部顯式呼叫，這層只做橋接與生命週期控管
 */
class FatigueScreenViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val cameraViewModel = CameraViewModel(application)
    private val fatigueViewModel = FatigueViewModel(application)

    /* ---------- Camera 狀態 ---------- */
    val faceLandmarks: StateFlow<FaceLandmarkerResult?> = cameraViewModel.faceLandmarks
    val errorMessage: StateFlow<String?> = cameraViewModel.errorMessage
    val isCameraReady: StateFlow<Boolean> = cameraViewModel.isCameraReady

    /* ---------- Fatigue 狀態（直接轉曝） ---------- */
    val fatigueLevel = fatigueViewModel.fatigueLevel
    val showFatigueDialog = fatigueViewModel.showFatigueDialog
    val statusText = fatigueViewModel.statusText
    val isFaceDetected = fatigueViewModel.isFaceDetected
    val uiEvent = fatigueViewModel.uiEvent

    val calibrationProgress = fatigueViewModel.calibrationProgress
    val isCalibrating = fatigueViewModel.isCalibrating
    val calibrationEarValue = fatigueViewModel.calibrationEarValue

    val blinkFrequency = fatigueViewModel.blinkFrequency
    val showBlinkFrequency = fatigueViewModel.showBlinkFrequency

    val yawnCount = fatigueViewModel.yawnCount
    val eyeClosureDuration = fatigueViewModel.eyeClosureDuration

    // 分數 0~100
    val fatigueScore = fatigueViewModel.fatigueScore

    /* ---------- 冪等/資源控管 ---------- */
    @Volatile private var hasInitialized = false
    private var lastBoundPreview: PreviewView? = null

    /**
     * 初始化相機與人臉 Landmark 串流；冪等：
     * - 同一個 PreviewView 重複呼叫不會再初始化
     * - 若換了 PreviewView（例如 Activity 重建），會重新綁定
     */
    fun initializeFatigueDetection(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
    ) {
        if (hasInitialized && lastBoundPreview === previewView) {
            // 已初始化且是同一個 PreviewView，忽略
            return
        }
        hasInitialized = true
        lastBoundPreview = previewView

        viewModelScope.launch {
            cameraViewModel.initializeCamera(
                previewView = previewView,
                lifecycleOwner = lifecycleOwner,
                onFaceLandmarksResult = { result ->
                    fatigueViewModel.processFaceLandmarks(result)
                },
            )
        }
    }

    /** 顯式開始/停止偵測（給畫面呼叫） */
    fun startDetection() = fatigueViewModel.startDetection()
    fun stopDetection()  = fatigueViewModel.stopDetection()

    /** 重新開始偵測（停止→開始） */
    fun restartDetection() {
        viewModelScope.launch {
            fatigueViewModel.stopDetection()
            fatigueViewModel.startDetection()
        }
    }

    /** 校正與對話框動作（轉呼叫） */
    fun startCalibration()         = fatigueViewModel.startCalibration()
    fun stopCalibration()          = fatigueViewModel.stopCalibration()
    fun onUserAcknowledged()       = fatigueViewModel.handleUserAcknowledged()
    fun onUserRequestedRest()      = fatigueViewModel.handleUserRequestedRest()
    fun clearError()               = cameraViewModel.clearError()
    fun isCameraReadyNow(): Boolean = isCameraReady.value
    fun getCameraStatus(): String   = cameraViewModel.getCameraStatus()

    /** 調試與參數（轉呼叫） */
    fun getResetStatusInfo(): String           = fatigueViewModel.getResetStatusInfo()
    fun generateDebugReport(): String          = fatigueViewModel.generateDebugReport()
    fun saveDebugReport(): String              = fatigueViewModel.saveDebugReport()
    fun setDebugMode(mode: String)             = fatigueViewModel.setDebugMode(mode)
    fun checkAutoReport(): String?             = fatigueViewModel.checkAutoReport()
    fun setMinProcessIntervalMs(ms: Long)      = fatigueViewModel.setMinProcessIntervalMs(ms)
    fun setProcessingRateFps(fps: Int)         = fatigueViewModel.setProcessingRateFps(fps)

    /** 釋放資源（相機 + 偵測） */
    fun shutdown() {
        viewModelScope.launch {
            try {
                fatigueViewModel.stopDetection()
            } catch (_: Throwable) { /* ignore */ }
            try {
                cameraViewModel.releaseCamera()
            } catch (_: Throwable) { /* ignore */ }
            hasInitialized = false
            lastBoundPreview = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        shutdown()
    }
}
