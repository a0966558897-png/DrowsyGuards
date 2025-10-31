package com.patrick.alert

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException

/**
 * 聲音管理器
 * 負責處理警告和緊急聲音的播放
 */
class SoundManager(private val context: Context) {
    companion object {
        private const val TAG = "SoundManager"
        const val SOUND_ALERT_DURATION_MS = 2000L // 聲音警報2秒
    }

    private var mediaPlayer: MediaPlayer? = null
    private val soundHandler = Handler(Looper.getMainLooper())

    /**
     * 播放警告聲音
     */
    fun playWarningSound() {
        try {
            releaseMediaPlayer()

            val soundResourceId = getWarningSoundResource()
            if (soundResourceId == 0) {
                Log.w(TAG, "警告音效資源未找到")
                return
            }

            mediaPlayer = MediaPlayer.create(context, soundResourceId)
            mediaPlayer?.let { player ->
                player.isLooping = false
                player.setOnCompletionListener {
                    releaseMediaPlayer()
                }
                player.start()

                // 2秒后停止声音
                soundHandler.postDelayed({
                    if (player.isPlaying) {
                        player.stop()
                    }
                }, SOUND_ALERT_DURATION_MS)
            } ?: run {
                Log.w(TAG, "MediaPlayer 創建失敗")
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "播放警告聲音時參數錯誤", e)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "播放警告聲音時狀態錯誤", e)
        } catch (e: IOException) {
            Log.e(TAG, "播放警告聲音時IO錯誤", e)
        } catch (e: Exception) {
            Log.e(TAG, "播放警告聲音時發生未知錯誤: ${e.message}", e)
        }
    }

    /**
     * 播放緊急警告聲音
     */
    fun playEmergencySound() {
        try {
            releaseMediaPlayer()

            val soundResourceId = getEmergencySoundResource()
            if (soundResourceId == 0) {
                Log.w(TAG, "緊急音效資源未找到")
                return
            }

            mediaPlayer = MediaPlayer.create(context, soundResourceId)
            mediaPlayer?.let { player ->
                player.isLooping = true // 緊急聲音循環播放
                player.setOnCompletionListener {
                    releaseMediaPlayer()
                }
                player.start()

                // 3秒后停止声音
                soundHandler.postDelayed({
                    if (player.isPlaying) {
                        player.stop()
                    }
                }, (SOUND_ALERT_DURATION_MS * 1.5).toLong())
            } ?: run {
                Log.w(TAG, "MediaPlayer 創建失敗")
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "播放緊急警告聲音時參數錯誤", e)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "播放緊急警告聲音時狀態錯誤", e)
        } catch (e: IOException) {
            Log.e(TAG, "播放緊急警告聲音時IO錯誤", e)
        } catch (e: Exception) {
            Log.e(TAG, "播放緊急警告聲音時發生未知錯誤: ${e.message}", e)
        }
    }

    /**
     * 停止所有聲音
     */
    fun stopAllSounds() {
        releaseMediaPlayer()
        soundHandler.removeCallbacksAndMessages(null)
    }

    /**
     * 獲取警告聲音資源ID
     */
    private fun getWarningSoundResource(): Int {
        return context.resources.getIdentifier("warning", "raw", "com.patrick.drowsyguard")
    }

    /**
     * 獲取緊急警告聲音資源ID
     */
    private fun getEmergencySoundResource(): Int {
        return context.resources.getIdentifier("emergency", "raw", "com.patrick.drowsyguard")
    }

    /**
     * 釋放MediaPlayer資源
     */
    private fun releaseMediaPlayer() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
    }
}
