package com.patrick.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.patrick.camera.ImageUtils.toBitmap
import com.patrick.detection.FaceLandmarkerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 攝像頭控制器
 * 負責攝像頭的具體操作，如綁定、解綁、縮放等
 * 遵循 Clean Architecture 的 Data 層原則
 */
class CameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var isBound = false
    private var camera: Camera? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var retryCount = 0
    private val maxRetries = 3
    private var errorCallback: ((String) -> Unit)? = null
    private var faceLandmarker: FaceLandmarker? = null
    private var onFaceLandmarksDetected: ((FaceLandmarkerResult) -> Unit)? = null

    companion object {
        private const val TAG = "CameraController"
    }

    /**
     * 設置錯誤回調
     */
    fun setErrorCallback(callback: (String) -> Unit) {
        this.errorCallback = callback
    }

    /**
     * 設置面部特徵點檢測回調
     */
    fun setFaceLandmarksCallback(callback: (FaceLandmarkerResult) -> Unit) {
        Log.d(TAG, "[CameraController] setFaceLandmarksCallback called, callback hash: ${callback.hashCode()}")
        this.onFaceLandmarksDetected = callback
        Log.d(TAG, "[CameraController] onFaceLandmarksDetected set to: non-null")
    }

    fun bindCamera() {
        if (isBound) {
            Log.d(TAG, "Camera already bound, skipping bind request")
            return
        }

        Log.d(TAG, "Starting camera binding process")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                if (cameraProvider == null) {
                    Log.e(TAG, "Failed to get camera provider")
                    errorCallback?.invoke("無法獲取攝像頭提供者")
                    return@addListener
                }

                setupCamera()
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
                handleCameraError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * 重新綁定攝像頭（用於配置變更）
     */
    fun rebindCamera() {
        Log.d(TAG, "Rebinding camera for configuration change")
        unbindCamera()
        // 延遲一點時間確保解綁完成
        CoroutineScope(Dispatchers.Main).launch {
            kotlinx.coroutines.delay(50)
            bindCamera()
        }
    }

    private fun setupCamera() {
        try {
            val rotation = previewView.display.rotation
            val preview =
                Preview.Builder()
                    .setTargetRotation(rotation)
                    .build()
                    .also {
                        Log.d(TAG, "[setupCamera] setSurfaceProvider, previewView hashCode: ${previewView.hashCode()}")
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

            // 創建圖像分析用例
            val imageAnalysis = createImageAnalysis()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // 先解綁所有用例
            cameraProvider?.unbindAll()
            Log.d(
                TAG,
                "[setupCamera] bindToLifecycle, previewView hashCode: ${previewView.hashCode()}, cameraProvider: ${cameraProvider != null}",
            )
            // 綁定新的用例
            camera =
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis,
                )

            if (camera != null) {
                isBound = true
                retryCount = 0
                Log.d(TAG, "Camera successfully bound with image analysis and preview")
            } else {
                Log.e(TAG, "Failed to bind camera - camera is null")
                handleCameraError(Exception("攝像頭綁定失敗"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupCamera", e)
            handleCameraError(e)
        }
    }

    private fun createImageAnalysis(): ImageAnalysis {
        Log.d(TAG, "[CameraController] createImageAnalysis called")
        // 創建支持實時處理的 FaceLandmarker
        faceLandmarker = FaceLandmarkerManager.createForRealTime(context)

        val minInterval = 100L
        var lastAnalyzedTimestamp = 0L
        val analysis =
            ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
            val now = System.currentTimeMillis()
            if (now - lastAnalyzedTimestamp > minInterval) {
                lastAnalyzedTimestamp = now
                // MediaPipe 推論與特徵計算放到 background thread
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
                    processImage(imageProxy)
                }
            } else {
                imageProxy.close()
            }
        }
        return analysis
    }

    private fun processImage(imageProxy: ImageProxy) {
        Log.d(
            TAG,
            "[CameraController] processImage called: ${imageProxy.width}x${imageProxy.height}, timestamp=${imageProxy.imageInfo.timestamp}, previewView hashCode: ${previewView.hashCode()}",
        )
        
        var bitmap: Bitmap? = null
        try {
            Log.d(TAG, "Processing image: ${imageProxy.width}x${imageProxy.height}")

            // 將 ImageProxy 轉換為 Bitmap
            bitmap = imageProxy.toBitmap()
            Log.d(TAG, "Bitmap created: ${bitmap.width}x${bitmap.height}")

            // 從 Bitmap 創建 MPImage
            val mpImage = BitmapImageBuilder(bitmap).build()
            Log.d(TAG, "MPImage created successfully")

            // 使用 MediaPipe 進行同步檢測
            val result = faceLandmarker?.detect(mpImage)
            Log.d(
                TAG,
                "[CameraController] MediaPipe detection result: ${result?.faceLandmarks()?.size ?: 0} faces detected, landmark0 size=${result?.faceLandmarks()?.getOrNull(0)?.size ?: -1}",
            )

            // 處理檢測結果
            result?.let {
                Log.d(
                    TAG,
                    "[CameraController] onFaceLandmarksDetected callback triggered, callback is ${if (onFaceLandmarksDetected != null) "non-null" else "null"}",
                )
                onFaceLandmarksDetected?.invoke(it)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing image with MediaPipe", e)
        } finally {
            // 重要：手動回收 Bitmap 避免記憶體洩漏
            bitmap?.let {
                if (!it.isRecycled) {
                    it.recycle()
                    Log.d(TAG, "Bitmap recycled successfully")
                }
            }
            // 手動關閉 imageProxy
            imageProxy.close()
        }
    }

    private fun handleCameraError(error: Exception) {
        Log.e(TAG, "Camera error occurred", error)
        retryCount++

        if (retryCount <= maxRetries) {
            Log.d(TAG, "Retrying camera binding, attempt $retryCount/$maxRetries")
            // 延遲重試
            CoroutineScope(Dispatchers.Main).launch {
                kotlinx.coroutines.delay(1000L * retryCount)
                bindCamera()
            }
        } else {
            Log.e(TAG, "Max retry attempts reached")
            errorCallback?.invoke("攝像頭初始化失敗，已重試 $maxRetries 次")
        }
    }

    private fun unbindCamera() {
        try {
            if (isBound) {
                cameraProvider?.unbindAll()
                isBound = false
                Log.d(TAG, "Camera unbound successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unbinding camera", e)
        }
    }

    /**
     * 設置攝像頭縮放
     */
    fun setZoom(zoomRatio: Float) {
        try {
            camera?.cameraControl?.setZoomRatio(zoomRatio)
            Log.d(TAG, "Camera zoom set to: $zoomRatio")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting camera zoom", e)
        }
    }

    /**
     * 釋放資源
     */
    fun release() {
        Log.d(TAG, "Releasing camera controller")
        unbindCamera()
        faceLandmarker?.close()
        faceLandmarker = null
        cameraExecutor.shutdown()
    }

    /**
     * 檢查攝像頭是否已綁定
     */
    fun isCameraReady(): Boolean {
        return isBound && camera != null
    }
}
