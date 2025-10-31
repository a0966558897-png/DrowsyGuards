package com.patrick.camera

import android.app.Application
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 相機 ViewModel
 * 負責相機初始化和面部特徵點檢測
 * 遵循單一職責原則
 */
class CameraViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val cameraUseCase: CameraUseCase = CameraModule.createCameraModule(application)

    private val _faceLandmarks = MutableStateFlow<FaceLandmarkerResult?>(null)
    val faceLandmarks: StateFlow<FaceLandmarkerResult?> = _faceLandmarks

    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * 初始化相機
     */
    fun initializeCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onFaceLandmarksResult: (FaceLandmarkerResult) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                cameraUseCase.setFaceLandmarksCallback { result ->
                    _faceLandmarks.value = result
                    onFaceLandmarksResult(result)
                }
                cameraUseCase.initializeCamera(previewView, lifecycleOwner)
                _isCameraReady.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "相機初始化失敗: ${e.message}"
                _isCameraReady.value = false
            }
        }
    }

    /**
     * 釋放相機資源
     */
    fun releaseCamera() {
        viewModelScope.launch {
            try {
                cameraUseCase.releaseCamera()
                _isCameraReady.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "相機釋放失敗: ${e.message}"
            }
        }
    }

    /**
     * 清除錯誤信息
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * 檢查相機是否準備就緒
     */
    fun isCameraReady(): Boolean {
        return _isCameraReady.value
    }

    /**
     * 獲取相機狀態信息
     */
    fun getCameraStatus(): String {
        return cameraUseCase.checkCameraStatus()
    }
}
