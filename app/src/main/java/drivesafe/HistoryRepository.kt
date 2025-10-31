package com.example.drivesafe

import android.content.Context
import com.example.drivesafe.db.AppDatabase
import com.example.drivesafe.db.FatigueRecord
import com.patrick.main.ui.FatigueUiItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class HistoryRepository private constructor(private val appContext: Context) {

    private val dao by lazy { AppDatabase.getInstance(appContext).fatigueDao() }

    fun recentFlow(limit: Int = 50): Flow<List<FatigueUiItem>> {
        return dao.recentFlow(limit).map { list -> list.map { it.toUiItem() } }
    }

    suspend fun recentOnce(limit: Int = 50): List<FatigueUiItem> = withContext(Dispatchers.IO) {
        dao.recent(limit).map { it.toUiItem() }
    }

    /** 將資料表的紀錄轉成 UI 模型；時間欄位採「有效時間」：
     *  detectedAt != 0 → 用 detectedAt；否則用 timestampMs（null 則 0L）
     *  分數 score 為 Float → 轉成 Int（四捨五入）
     */
    private fun FatigueRecord.toUiItem(): FatigueUiItem {
        val id = this.id

        // score: Float -> Int（四捨五入；你也可改用 toInt() 直接截斷）
        val scoreInt: Int = try {
            // Java primitive float/Float 皆可
            this.score.let { f -> if (f is Number) f.toFloat().roundToInt() else 0 }
        } catch (_: Throwable) {
            0
        }

        val detectedAt = this.detectedAt              // long (primitive) 或 Long（Kotlin 會當 Long）
        val tsMs: Long? = this.timestampMs            // 可能為 null
        val effectiveTs: Long = if (detectedAt != 0L) detectedAt else (tsMs ?: 0L)

        val levelLabel = when {
            scoreInt >= 70 -> "高風險"
            scoreInt >= 30 -> "偏高"
            else -> "正常"
        }

        return FatigueUiItem(
            id = id,
            timestampMillis = effectiveTs,
            score = scoreInt,
            levelLabel = levelLabel,
            detail = null
        )
    }

    companion object {
        @Volatile private var INSTANCE: HistoryRepository? = null
        fun get(context: Context): HistoryRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: HistoryRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
