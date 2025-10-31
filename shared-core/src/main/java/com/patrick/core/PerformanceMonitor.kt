package com.patrick.core

import android.content.Context
import android.os.StrictMode
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 性能監控工具
 * 用於監控應用程序的性能指標
 */
class PerformanceMonitor private constructor(private val context: Context) {
    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val LOG_FILE_NAME = "performance_log.txt"
        private const val MAX_LOG_SIZE = 1024 * 1024 // 1MB
        private const val MAX_LOG_LINES = 1000

        @Volatile
        private var INSTANCE: PerformanceMonitor? = null

        fun getInstance(context: Context): PerformanceMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PerformanceMonitor(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    /**
     * 初始化性能監控
     */
    fun initialize() {
        setupStrictMode()
        logPerformance("PerformanceMonitor initialized")
    }

    /**
     * 設置嚴格模式
     */
    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build(),
            )

            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .penaltyLog()
                    .build(),
            )
        }
    }

    /**
     * 記錄性能指標
     */
    fun logPerformance(
        message: String,
        data: Map<String, Any>? = null,
    ) {
        scope.launch {
            val timestamp = dateFormat.format(Date())
            val logEntry =
                buildString {
                    append("[$timestamp] $message")
                    data?.forEach { (key, value) ->
                        append(" | $key: $value")
                    }
                }

            Log.d(TAG, logEntry)
            writeToFile(logEntry)
        }
    }

    /**
     * 記錄內存使用情況
     */
    fun logMemoryUsage() {
        scope.launch {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            val memoryUsage = (usedMemory.toFloat() / maxMemory.toFloat()) * 100

            logPerformance(
                "Memory Usage",
                mapOf(
                    "used" to "${usedMemory / 1024 / 1024}MB",
                    "max" to "${maxMemory / 1024 / 1024}MB",
                    "usage" to "${"%.1f".format(memoryUsage)}%",
                ),
            )
        }
    }

    /**
     * 記錄方法執行時間
     */
    suspend fun <T> measureExecutionTime(
        operationName: String,
        operation: suspend () -> T,
    ): T {
        val startTime = System.currentTimeMillis()
        return try {
            val result = operation()
            val executionTime = System.currentTimeMillis() - startTime
            logPerformance(
                "Operation completed",
                mapOf(
                    "operation" to operationName,
                    "duration" to "${executionTime}ms",
                ),
            )
            result
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            logPerformance(
                "Operation failed",
                mapOf(
                    "operation" to operationName,
                    "duration" to "${executionTime}ms",
                    "error" to (e.message ?: "Unknown error"),
                ),
            )
            throw e
        }
    }

    /**
     * 記錄 CPU 使用情況
     */
    fun logCpuUsage() {
        scope.launch {
            val cpuUsage = getCpuUsage()
            logPerformance(
                "CPU Usage",
                mapOf("usage" to "${"%.1f".format(cpuUsage)}%"),
            )
        }
    }

    /**
     * 獲取 CPU 使用率
     */
    private fun getCpuUsage(): Float {
        return try {
            val reader = File("/proc/stat").readText()
            val lines = reader.split("\n")
            if (lines.isNotEmpty()) {
                val cpuLine = lines[0]
                val values = cpuLine.split("\\s+".toRegex())
                if (values.size >= 5) {
                    val user = values[1].toLong()
                    val nice = values[2].toLong()
                    val system = values[3].toLong()
                    val idle = values[4].toLong()

                    val total = user + nice + system + idle
                    val usage = (user + nice + system).toFloat() / total.toFloat() * 100
                    usage
                } else {
                    0f
                }
            } else {
                0f
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get CPU usage", e)
            0f
        }
    }

    /**
     * 寫入日誌文件
     */
    private suspend fun writeToFile(logEntry: String) {
        withContext(Dispatchers.IO) {
            try {
                val logFile = File(context.filesDir, LOG_FILE_NAME)
                FileWriter(logFile, true).use { writer ->
                    PrintWriter(writer).use { printer ->
                        printer.println(logEntry)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write to log file", e)
            }
        }
    }

    /**
     * 清理舊的日誌文件
     */
    fun cleanupOldLogs() {
        scope.launch {
            try {
                val logFile = File(context.filesDir, LOG_FILE_NAME)
                if (logFile.exists() && logFile.length() > MAX_LOG_SIZE) {
                    val lines = logFile.readLines()
                    val recentLines = lines.takeLast(MAX_LOG_LINES)
                    logFile.writeText(recentLines.joinToString("\n"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cleanup old logs", e)
            }
        }
    }
}
