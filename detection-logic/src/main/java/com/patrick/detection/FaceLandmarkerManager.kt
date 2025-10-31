package com.patrick.detection

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker.FaceLandmarkerOptions
import com.patrick.core.Constants

object FaceLandmarkerManager {
    @Volatile private var faceLandmarker: FaceLandmarker? = null
    private var lastUsedTimestamp: Long = 0L
    private const val IDLE_TIMEOUT_MS = 10_000L
    private val handler = Handler(Looper.getMainLooper())
    private var idleCheckRunnable: Runnable? = null

    @Synchronized
    fun get(context: Context): FaceLandmarker {
        lastUsedTimestamp = System.currentTimeMillis()
        if (faceLandmarker == null) {
            val baseOptions = BaseOptions.builder()
                .setDelegate(Delegate.CPU)
                .setModelAssetPath(Constants.FACE_LANDMARKER_MODEL_PATH)
                .build()
            val options = FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)   // 同步 detect() → 用 IMAGE
                .setMinFaceDetectionConfidence(0.3f)
                .setMinTrackingConfidence(0.3f)
                .setMinFacePresenceConfidence(0.3f)
                .setNumFaces(1)
                .setOutputFaceBlendshapes(true)      // 打呵欠需要
                .build()
            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
            scheduleIdleCheck()
        }
        return faceLandmarker!!
    }

    @Synchronized
    fun createForRealTime(context: Context): FaceLandmarker {
        val baseOptions = BaseOptions.builder()
            .setDelegate(Delegate.CPU)
            .setModelAssetPath(Constants.FACE_LANDMARKER_MODEL_PATH)
            .build()
        val options = FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE) // 同步 detect() → 用 IMAGE
            .setMinFaceDetectionConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setMinFacePresenceConfidence(0.5f)
            .setNumFaces(1)
            .setOutputFaceBlendshapes(true)
            .build()
        return FaceLandmarker.createFromOptions(context, options)
    }

    @Synchronized
    fun release() {
        faceLandmarker?.close()
        faceLandmarker = null
        idleCheckRunnable?.let { handler.removeCallbacks(it) }
        idleCheckRunnable = null
    }

    @Synchronized
    fun maybeReleaseIfIdle() {
        val idleTime = System.currentTimeMillis() - lastUsedTimestamp
        if (idleTime >= IDLE_TIMEOUT_MS) release()
    }

    @Synchronized
    fun releaseOnAppExit() { release() }

    private fun scheduleIdleCheck() {
        idleCheckRunnable?.let { handler.removeCallbacks(it) }
        idleCheckRunnable = Runnable {
            maybeReleaseIfIdle()
            if (faceLandmarker != null) {
                handler.postDelayed(idleCheckRunnable!!, IDLE_TIMEOUT_MS)
            }
        }
        handler.postDelayed(idleCheckRunnable!!, IDLE_TIMEOUT_MS)
    }
}
