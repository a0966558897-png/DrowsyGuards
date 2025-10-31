package com.patrick.camera

import android.content.Context
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

/**
 * 攝像頭管理器
 * 負責攝像頭的高級操作和狀態管理
 * 遵循 Clean Architecture 的 Domain 層原則
 */
class CameraManager(
    private val context: Context,
) {
    companion object {
        private const val TAG = "CameraManager"
    }

    private var cameraController: CameraController? = null
    private var onFaceLandmarksDetected: ((FaceLandmarkerResult) -> Unit)? = null

    /**
     * 設置面部特徵點檢測回調
     */
    fun setFaceLandmarksCallback(callback: (FaceLandmarkerResult) -> Unit) {
        Log.d(TAG, "[CameraManager] setFaceLandmarksCallback called")
        this.onFaceLandmarksDetected = callback
    }

    /**
     * 初始化攝像頭
     */
    fun initializeCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
    ) {
        Log.d(TAG, "[CameraManager] initializeCamera called with previewView: ${previewView.width}x${previewView.height}")

        try {
            Log.d(TAG, "[CameraManager] Creating CameraController")
            cameraController = CameraController(context, lifecycleOwner, previewView)
            Log.d(TAG, "[CameraManager] CameraController created successfully")

            // 設置錯誤回調
            cameraController?.setErrorCallback { error ->
                Log.e(TAG, "Camera error: $error")
            }

            // 設置面部特徵點檢測回調
            cameraController?.setFaceLandmarksCallback { result ->
                Log.d(TAG, "[CameraManager] cameraController.onFaceLandmarksDetected triggered")
                onFaceLandmarksDetected?.invoke(result)
            }

            // 綁定攝像頭（在設置 callback 之後）
            Log.d(TAG, "[CameraManager] Binding camera")
            cameraController?.bindCamera()
            Log.d(TAG, "[CameraManager] Camera bound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize camera", e)
        }
    }

    /**
     * 重新綁定攝像頭
     */
    fun rebindCamera() {
        Log.d(TAG, "Rebinding camera")
        cameraController?.rebindCamera()
    }

    /**
     * 釋放攝像頭資源
     */
    fun releaseCamera() {
        Log.d(TAG, "Releasing camera")
        cameraController?.release()
        cameraController = null
    }

    /**
     * 檢查攝像頭是否已準備就緒
     */
    fun isCameraReady(): Boolean {
        return cameraController?.isCameraReady() == true
    }
}
