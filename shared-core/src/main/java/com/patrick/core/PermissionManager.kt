package com.patrick.core

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * 通用權限管理器
 * 負責權限的檢查、請求和狀態管理
 * 遵循 Clean Architecture 的 Data 層原則
 */
class PermissionManager(private val activity: ComponentActivity) {
    companion object {
        private const val TAG = "PermissionManager"
    }

    private var permissionCallback: ((Boolean) -> Unit)? = null

    // 權限請求回調
    private val requestPermissionLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            Log.d(TAG, "Permission result: $isGranted")
            permissionCallback?.invoke(isGranted)
        }

    /**
     * 檢查權限
     * @param permission 權限字符串
     * @return true 如果權限已授予，false 否則
     */
    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 請求權限
     * @param permission 權限字符串
     * @param callback 權限請求結果回調
     */
    fun requestPermission(
        permission: String,
        callback: (Boolean) -> Unit,
    ) {
        permissionCallback = callback

        if (hasPermission(permission)) {
            Log.d(TAG, "Permission already granted: $permission")
            callback(true)
            return
        }

        Log.d(TAG, "Requesting permission: $permission")
        requestPermissionLauncher.launch(permission)
    }

    /**
     * 檢查攝像頭權限
     * @return true 如果權限已授予，false 否則
     */
    fun hasCameraPermission(): Boolean {
        return hasPermission(Manifest.permission.CAMERA)
    }

    /**
     * 請求攝像頭權限
     * @param callback 權限請求結果回調
     */
    fun requestCameraPermission(callback: (Boolean) -> Unit) {
        requestPermission(Manifest.permission.CAMERA, callback)
    }

    /**
     * 檢查震動權限
     * @return true 如果權限已授予，false 否則
     */
    fun hasVibratePermission(): Boolean {
        return hasPermission(Manifest.permission.VIBRATE)
    }

    /**
     * 請求震動權限
     * @param callback 權限請求結果回調
     */
    fun requestVibratePermission(callback: (Boolean) -> Unit) {
        requestPermission(Manifest.permission.VIBRATE, callback)
    }

    /**
     * 檢查權限是否應該顯示說明
     * @return true 如果應該顯示說明
     */
    fun shouldShowPermissionRationale(permission: String): Boolean {
        // 對於 ComponentActivity，我們無法直接檢查 shouldShowRequestPermissionRationale
        // 但我們可以通過其他方式處理，比如檢查權限是否被永久拒絕
        return !hasPermission(permission)
    }
}
