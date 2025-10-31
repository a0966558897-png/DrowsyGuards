package com.patrick.camera

import android.content.Context

/**
 * 攝像頭模組依賴注入工廠
 * 提供 Clean Architecture 組件的實例化
 * 遵循依賴反轉原則
 */
object CameraModule {
    /**
     * 創建攝像頭倉儲
     */
    fun createCameraRepository(context: android.content.Context): CameraRepository {
        return CameraRepositoryImpl(context)
    }

    /**
     * 創建攝像頭用例
     */
    fun createCameraUseCase(cameraRepository: CameraRepository): CameraUseCase {
        return CameraUseCase(cameraRepository)
    }

    /**
     * 創建完整的攝像頭模組
     */
    fun createCameraModule(context: android.content.Context): CameraUseCase {
        val repository = createCameraRepository(context)
        return createCameraUseCase(repository)
    }
}
