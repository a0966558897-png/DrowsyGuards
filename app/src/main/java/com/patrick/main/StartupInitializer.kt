package com.patrick.main

import android.content.Context
import androidx.startup.Initializer
import com.patrick.core.AsyncTaskManager
import com.patrick.core.PerformanceMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 啟動優化初始化器
 * 使用 AndroidX Startup 進行懶加載和背景初始化
 */
class StartupInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val performanceMonitor = PerformanceMonitor.getInstance(context)
        performanceMonitor.logPerformance("App startup began")

        // 初始化性能監控
        performanceMonitor.initialize()

        // 在背景線程中進行非關鍵初始化
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 預初始化異步任務管理器
                AsyncTaskManager.getInstance(context)

                // 預加載常用資源
                preloadResources(context)

                // 初始化對象池
                initializeObjectPools(context)

                performanceMonitor.logPerformance("Background initialization completed")
            } catch (e: Exception) {
                performanceMonitor.logPerformance(
                    "Background initialization failed",
                    mapOf("error" to (e.message ?: "Unknown error")),
                )
            }
        }

        performanceMonitor.logPerformance("App startup completed")
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

    /**
     * 預加載資源
     */
    private suspend fun preloadResources(context: Context) {
        // 預加載字體
        try {
            context.assets.open("fonts/NotoSansCJK-Regular.ttc").use { stream ->
                stream.read()
            }
        } catch (e: Exception) {
            // 忽略字體加載失敗
        }

        // 預加載音頻資源
        try {
            context.resources.openRawResourceFd(
                context.resources.getIdentifier("warning", "raw", context.packageName),
            )?.use { fd ->
                fd.close()
            }
        } catch (e: Exception) {
            // 忽略音頻資源加載失敗
        }
    }

    /**
     * 初始化對象池
     */
    private suspend fun initializeObjectPools(context: Context) {
        // 這裡可以初始化各種對象池
        // 例如：BitmapPool、ByteArrayPool 等
    }
}
