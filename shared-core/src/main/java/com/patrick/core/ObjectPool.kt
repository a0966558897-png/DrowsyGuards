package com.patrick.core

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 對象池管理工具
 * 用於減少對象創建和垃圾回收的開銷
 */
class ObjectPool<T>(
    private val maxSize: Int,
    private val factory: () -> T,
    private val reset: (T) -> Unit = {},
) {
    companion object {
        private const val TAG = "ObjectPool"
    }

    private val pool = ConcurrentLinkedQueue<T>()
    private val mutex = Mutex()
    private var createdCount = 0
    private var borrowedCount = 0
    private var returnedCount = 0

    /**
     * 獲取對象
     */
    suspend fun borrow(): T =
        mutex.withLock {
            val obj = pool.poll()
            if (obj != null) {
                borrowedCount++
                Log.v(TAG, "Borrowed object from pool. Pool size: ${pool.size}")
                obj
            } else {
                if (createdCount < maxSize) {
                    createdCount++
                    borrowedCount++
                    Log.v(TAG, "Created new object. Total created: $createdCount")
                    factory()
                } else {
                    throw IllegalStateException("Object pool is full and no objects available")
                }
            }
        }

    /**
     * 歸還對象
     */
    suspend fun returnObject(obj: T) =
        mutex.withLock {
            if (pool.size < maxSize) {
                reset(obj)
                pool.offer(obj)
                returnedCount++
                Log.v(TAG, "Returned object to pool. Pool size: ${pool.size}")
            } else {
                Log.w(TAG, "Pool is full, discarding object")
            }
        }

    /**
     * 獲取池統計信息
     */
    suspend fun getStats(): PoolStats =
        mutex.withLock {
            PoolStats(
                poolSize = pool.size,
                createdCount = createdCount,
                borrowedCount = borrowedCount,
                returnedCount = returnedCount,
                maxSize = maxSize,
            )
        }

    /**
     * 清空池
     */
    suspend fun clear() =
        mutex.withLock {
            pool.clear()
            Log.i(TAG, "Object pool cleared")
        }

    /**
     * 預填充池
     */
    suspend fun prefill(count: Int) =
        mutex.withLock {
            val fillCount = minOf(count, maxSize - pool.size)
            repeat(fillCount) {
                pool.offer(factory())
                createdCount++
            }
            Log.i(TAG, "Prefilled pool with $fillCount objects")
        }

    data class PoolStats(
        val poolSize: Int,
        val createdCount: Int,
        val borrowedCount: Int,
        val returnedCount: Int,
        val maxSize: Int,
    ) {
        val utilizationRate: Float = if (maxSize > 0) poolSize.toFloat() / maxSize else 0f
        val efficiencyRate: Float = if (borrowedCount > 0) returnedCount.toFloat() / borrowedCount else 0f
    }
}

/**
 * 位圖對象池
 * 專門用於管理 Bitmap 對象
 */
class BitmapPool(
    maxSize: Int = 10,
) {
    private val pool =
        ObjectPool<android.graphics.Bitmap>(
            maxSize = maxSize,
            factory = { android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888) },
            reset = { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            },
        )

    suspend fun borrow(): android.graphics.Bitmap = pool.borrow()

    suspend fun returnObject(bitmap: android.graphics.Bitmap) = pool.returnObject(bitmap)

    suspend fun getStats(): ObjectPool.PoolStats = pool.getStats()

    suspend fun clear() = pool.clear()

    suspend fun prefill(count: Int) = pool.prefill(count)
}

/**
 * 字節數組對象池
 * 專門用於管理字節數組
 */
class ByteArrayPool(
    private val bufferSize: Int,
    maxSize: Int = 20,
) {
    private val pool =
        ObjectPool<ByteArray>(
            maxSize = maxSize,
            factory = { ByteArray(bufferSize) },
            reset = { bytes ->
                bytes.fill(0)
            },
        )

    suspend fun borrow(): ByteArray = pool.borrow()

    suspend fun returnObject(bytes: ByteArray) = pool.returnObject(bytes)

    suspend fun getStats(): ObjectPool.PoolStats = pool.getStats()

    suspend fun clear() = pool.clear()

    suspend fun prefill(count: Int) = pool.prefill(count)
}

/**
 * 字符串構建器對象池
 * 專門用於管理 StringBuilder 對象
 */
class StringBuilderPool(
    maxSize: Int = 15,
) {
    private val pool =
        ObjectPool<StringBuilder>(
            maxSize = maxSize,
            factory = { StringBuilder() },
            reset = { builder ->
                builder.clear()
            },
        )

    suspend fun borrow(): StringBuilder = pool.borrow()

    suspend fun returnObject(builder: StringBuilder) = pool.returnObject(builder)

    suspend fun getStats(): ObjectPool.PoolStats = pool.getStats()

    suspend fun clear() = pool.clear()

    suspend fun prefill(count: Int) = pool.prefill(count)
}
