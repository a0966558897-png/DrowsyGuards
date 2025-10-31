package com.patrick.core

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import androidx.work.ListenableWorker.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * 異步任務管理器
 * 用於管理後台任務和優化性能
 */
class AsyncTaskManager private constructor(private val context: Context) {
    companion object {
        private const val TAG = "AsyncTaskManager"

        @Volatile
        private var INSTANCE: AsyncTaskManager? = null

        fun getInstance(context: Context): AsyncTaskManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AsyncTaskManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * 執行輕量級異步任務
     */
    fun executeLightTask(
        taskName: String,
        task: suspend () -> Unit,
    ) {
        scope.launch {
            try {
                PerformanceMonitor.getInstance(context).measureExecutionTime(taskName) {
                    task()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Light task failed: $taskName", e)
            }
        }
    }

    /**
     * 執行重量級後台任務
     */
    fun executeHeavyTask(
        taskName: String,
        @Suppress("UNUSED_PARAMETER") task: suspend () -> Unit,
        constraints: Constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build(),
    ) {
        val workRequest =
            OneTimeWorkRequestBuilder<HeavyTaskWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf("taskName" to taskName))
                .build()

        workManager.enqueue(workRequest)
        Log.d(TAG, "Enqueued heavy task: $taskName")
    }

    /**
     * 執行延遲任務
     */
    fun executeDelayedTask(
        taskName: String,
        delayMillis: Long,
        @Suppress("UNUSED_PARAMETER") task: suspend () -> Unit,
    ) {
        val workRequest =
            OneTimeWorkRequestBuilder<DelayedTaskWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("taskName" to taskName))
                .build()

        workManager.enqueue(workRequest)
        Log.d(TAG, "Enqueued delayed task: $taskName (delay: ${delayMillis}ms)")
    }

    /**
     * 執行週期性任務
     */
    fun executePeriodicTask(
        taskName: String,
        intervalMinutes: Long,
        @Suppress("UNUSED_PARAMETER") task: suspend () -> Unit,
        constraints: Constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build(),
    ) {
        val workRequest =
            PeriodicWorkRequestBuilder<PeriodicTaskWorker>(
                intervalMinutes,
                TimeUnit.MINUTES,
            )
                .setConstraints(constraints)
                .setInputData(workDataOf("taskName" to taskName))
                .build()

        workManager.enqueueUniquePeriodicWork(
            taskName,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest,
        )
        Log.d(TAG, "Enqueued periodic task: $taskName (interval: ${intervalMinutes}min)")
    }

    /**
     * 取消任務
     */
    fun cancelTask(taskName: String) {
        workManager.cancelUniqueWork(taskName)
        Log.d(TAG, "Cancelled task: $taskName")
    }

    /**
     * 取消所有任務
     */
    fun cancelAllTasks() {
        workManager.cancelAllWork()
        scope.cancel()
        Log.d(TAG, "Cancelled all tasks")
    }

    /**
     * 獲取任務狀態
     */
    fun getTaskStatus(
        taskName: String,
        callback: (WorkInfo.State) -> Unit,
    ) {
        // 簡化實現，直接獲取當前狀態
        scope.launch {
            try {
                val workInfos = workManager.getWorkInfosForUniqueWork(taskName).get()
                val state = workInfos.firstOrNull()?.state ?: WorkInfo.State.CANCELLED
                callback(state)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get task status for: $taskName", e)
                callback(WorkInfo.State.FAILED)
            }
        }
    }

    /**
     * 清理完成的工作
     */
    fun cleanupCompletedWork() {
        scope.launch {
            try {
                val workInfos = workManager.getWorkInfosByTag("completed").get()
                workInfos.forEach { workInfo ->
                    if (workInfo.state.isFinished) {
                        workManager.pruneWork()
                    }
                }
                Log.d(TAG, "Cleaned up completed work")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cleanup completed work", e)
            }
        }
    }

    /**
     * 重量級任務 Worker
     */
    class HeavyTaskWorker(
        context: Context,
        params: WorkerParameters,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val taskName = inputData.getString("taskName") ?: "Unknown"

            return try {
                PerformanceMonitor.getInstance(applicationContext).measureExecutionTime(taskName) {
                    // 實際任務邏輯在這裡實現
                    delay(100) // 模擬工作
                }
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Heavy task failed: $taskName", e)
                Result.failure()
            }
        }
    }

    /**
     * 延遲任務 Worker
     */
    class DelayedTaskWorker(
        context: Context,
        params: WorkerParameters,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val taskName = inputData.getString("taskName") ?: "Unknown"

            return try {
                PerformanceMonitor.getInstance(applicationContext).measureExecutionTime(taskName) {
                    // 實際任務邏輯在這裡實現
                    delay(50) // 模擬工作
                }
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Delayed task failed: $taskName", e)
                Result.failure()
            }
        }
    }

    /**
     * 週期性任務 Worker
     */
    class PeriodicTaskWorker(
        context: Context,
        params: WorkerParameters,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val taskName = inputData.getString("taskName") ?: "Unknown"

            return try {
                PerformanceMonitor.getInstance(applicationContext).measureExecutionTime(taskName) {
                    // 實際任務邏輯在這裡實現
                    delay(200) // 模擬工作
                }
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Periodic task failed: $taskName", e)
                Result.failure()
            }
        }
    }
}
