package com.patrick.alert

import android.content.Context
import android.util.Log

/**
 * 震動管理器
 * 負責處理震動提醒
 */
class VibrationManager(private val context: Context) {
    companion object {
        private const val TAG = "VibrationManager"
        const val VIBRATION_DURATION_MS = 500L // 震動0.5秒
    }

    /**
     * 觸發震動提醒
     */
    fun triggerVibration() {
        try {
            // 檢查震動權限
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(android.Manifest.permission.VIBRATE) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    Log.w(TAG, "缺少震動權限")
                    return
                }
            }

            val vibrator = getVibrator()

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val effect =
                    android.os.VibrationEffect.createOneShot(
                        VIBRATION_DURATION_MS,
                        android.os.VibrationEffect.DEFAULT_AMPLITUDE,
                    )
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(VIBRATION_DURATION_MS)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "觸發震動時權限錯誤", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "觸發震動時參數錯誤", e)
        } catch (e: Exception) {
            Log.e(TAG, "觸發震動時發生未知錯誤: ${e.message}", e)
        }
    }

    /**
     * 觸發強烈震動提醒
     */
    fun triggerStrongVibration() {
        try {
            // 檢查震動權限
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(android.Manifest.permission.VIBRATE) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    Log.w(TAG, "缺少震動權限")
                    return
                }
            }

            val vibrator = getVibrator()

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // 創建震動模式：震動500ms，暫停200ms，再震動500ms
                val pattern = longArrayOf(0, 500, 200, 500)
                val amplitudes = intArrayOf(0, 255, 0, 255)
                val effect = android.os.VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "觸發強烈震動時權限錯誤", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "觸發強烈震動時參數錯誤", e)
        } catch (e: Exception) {
            Log.e(TAG, "觸發強烈震動時發生未知錯誤: ${e.message}", e)
        }
    }

    /**
     * 獲取震動器實例
     */
    private fun getVibrator(): android.os.Vibrator {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        }
    }
}
