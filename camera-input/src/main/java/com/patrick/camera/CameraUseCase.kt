package com.patrick.camera

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.flow.StateFlow

/**
 * 攝像頭用例
 * 處理攝像頭相關的業務邏輯
 * 遵循 Clean Architecture 的 Domain 層原則
 */
class CameraUseCase(
    private val repository: CameraRepository,
) {
    /**
     * 攝像頭狀態
     */
    val cameraState: StateFlow<CameraRepository.CameraState> = repository.getCameraState()

    /**
     * 錯誤信息
     */
    val errorMessage: StateFlow<String?> = repository.getErrorMessage()

    /**
     * 初始化攝像頭
     */
    suspend fun initializeCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
    ) {
        repository.initializeCamera(previewView, lifecycleOwner)
    }

    /**
     * 重新綁定攝像頭
     */
    suspend fun rebindCamera() {
        repository.rebindCamera()
    }

    /**
     * 釋放攝像頭
     */
    suspend fun releaseCamera() {
        repository.releaseCamera()
    }

    /**
     * 檢查攝像頭是否準備就緒
     */
    fun isCameraReady(): Boolean {
        return repository.isCameraReady()
    }

    /**
     * 清除錯誤信息
     */
    fun clearError() {
        repository.clearError()
    }

    /**
     * 檢查攝像頭狀態
     */
    fun checkCameraStatus(): String {
        return repository.checkCameraStatus()
    }

    /**
     * 設置面部特徵點檢測回調
     */
    fun setFaceLandmarksCallback(callback: (FaceLandmarkerResult) -> Unit) {
        repository.setFaceLandmarksCallback(callback)
    }
}
