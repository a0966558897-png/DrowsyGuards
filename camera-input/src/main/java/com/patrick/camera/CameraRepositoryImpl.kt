package com.patrick.camera

import android.content.Context
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 攝像頭倉庫實現
 * 實現攝像頭操作的具體邏輯
 * 遵循 Clean Architecture 的 Data 層原則
 */
class CameraRepositoryImpl(
    private val context: Context,
) : CameraRepository {
    companion object {
        private const val TAG = "CameraRepositoryImpl"
    }

    private var cameraManager: CameraManager? = null
    private var onFaceLandmarksDetected: ((FaceLandmarkerResult) -> Unit)? = null

    // 狀態流
    private val _cameraState = MutableStateFlow(CameraRepository.CameraState.UNINITIALIZED)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _cameraStateFlow = _cameraState.asStateFlow()
    private val _errorMessageFlow = _errorMessage.asStateFlow()

    override suspend fun initializeCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
    ) {
        try {
            Log.d(TAG, "Initializing camera")
            _cameraState.value = CameraRepository.CameraState.INITIALIZING

            // 創建攝像頭管理器
            cameraManager = CameraManager(context)

            // 設置面部特徵點檢測回調
            cameraManager?.setFaceLandmarksCallback { result ->
                Log.d(
                    TAG,
                    "[CameraRepositoryImpl] cameraManager callback triggered, onFaceLandmarksDetected is ${if (onFaceLandmarksDetected != null) "non-null" else "null"}",
                )
                onFaceLandmarksDetected?.invoke(result)
            }

            // 初始化攝像頭
            cameraManager?.initializeCamera(previewView, lifecycleOwner)

            _cameraState.value = CameraRepository.CameraState.READY
            _errorMessage.value = null

            Log.d(TAG, "Camera initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize camera", e)
            _cameraState.value = CameraRepository.CameraState.ERROR
            _errorMessage.value = "攝像頭初始化失敗: ${e.message}"
        }
    }

    override suspend fun rebindCamera() {
        try {
            Log.d(TAG, "Rebinding camera")
            cameraManager?.rebindCamera()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rebind camera", e)
            _errorMessage.value = "攝像頭重新綁定失敗: ${e.message}"
        }
    }

    override suspend fun releaseCamera() {
        try {
            Log.d(TAG, "Releasing camera")
            cameraManager?.releaseCamera()
            cameraManager = null
            _cameraState.value = CameraRepository.CameraState.UNINITIALIZED
            _errorMessage.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release camera", e)
            _errorMessage.value = "攝像頭釋放失敗: ${e.message}"
        }
    }

    override fun isCameraReady(): Boolean {
        return cameraManager?.isCameraReady() == true
    }

    override fun getCameraState(): StateFlow<CameraRepository.CameraState> {
        return _cameraStateFlow
    }

    override fun getErrorMessage(): StateFlow<String?> {
        return _errorMessageFlow
    }

    override fun clearError() {
        _errorMessage.value = null
    }

    override fun checkCameraStatus(): String {
        return buildString {
            appendLine("攝像頭狀態檢查:")
            appendLine("- 狀態: ${_cameraState.value}")
            appendLine("- 管理器: ${if (cameraManager != null) "已創建" else "未創建"}")
            appendLine("- 準備就緒: ${isCameraReady()}")
            appendLine("- 錯誤: ${_errorMessage.value ?: "無"}")
        }
    }

    override fun setFaceLandmarksCallback(callback: (FaceLandmarkerResult) -> Unit) {
        Log.d(TAG, "[CameraRepositoryImpl] setFaceLandmarksCallback called")
        this.onFaceLandmarksDetected = callback
    }
}
