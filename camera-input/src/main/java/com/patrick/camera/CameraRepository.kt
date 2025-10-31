package com.patrick.camera

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.flow.StateFlow

/**
 * 攝像頭倉庫接口
 * 定義攝像頭操作的抽象接口
 * 遵循 Clean Architecture 的 Domain 層原則
 */
interface CameraRepository {
    /**
     * 攝像頭狀態枚舉
     */
    enum class CameraState {
        UNINITIALIZED, // 未初始化
        INITIALIZING, // 初始化中
        READY, // 準備就緒
        ERROR, // 錯誤狀態
        PERMISSION_REQUIRED, // 需要權限
    }

    /**
     * 初始化攝像頭
     */
    suspend fun initializeCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
    )

    /**
     * 重新綁定攝像頭
     */
    suspend fun rebindCamera()

    /**
     * 釋放攝像頭
     */
    suspend fun releaseCamera()

    /**
     * 檢查攝像頭是否準備就緒
     */
    fun isCameraReady(): Boolean

    /**
     * 獲取攝像頭狀態
     */
    fun getCameraState(): StateFlow<CameraState>

    /**
     * 獲取錯誤信息
     */
    fun getErrorMessage(): StateFlow<String?>

    /**
     * 清除錯誤信息
     */
    fun clearError()

    /**
     * 檢查攝像頭狀態
     */
    fun checkCameraStatus(): String

    /**
     * 設置面部特徵點檢測回調
     */
    fun setFaceLandmarksCallback(callback: (FaceLandmarkerResult) -> Unit)
}
