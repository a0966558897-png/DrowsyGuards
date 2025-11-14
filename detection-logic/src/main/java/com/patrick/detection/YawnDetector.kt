package com.patrick.detection

import android.util.Log

/**
 * 張嘴偵測（當作打呵欠）：
 * - 來源：FaceLandmarker blendshapes 的 jawOpen / mouthFunnel 分數 (0..1)
 * - 低通濾波（EMA）+ 持續時間門檻（openHoldMs 毫秒）+ 冷卻（cooldownMs）
 * - 用動態 baseline：最近一段時間的「低值」當基線，門檻 = baseline * kOverBaseline
 *
 * FatigueDetectionManager 會呼叫：
 *   val yd = yawnDetector.update(openScore, now)
 *   if (yd.yawnTriggered) { ... }
 */
class YawnDetector(
    // 低通濾波係數：越大反應越快，越小越平滑
    private val alpha: Float = 0.30f,
    // 需維持「張口」多久才算一次（毫秒）
    private val openHoldMs: Long = 700L,
    // 事件觸發後的冷卻時間（毫秒）
    private val cooldownMs: Long = 2000L,
    // 基線緩慢追蹤的學習率
    private val baselineAlpha: Float = 0.03f,
    // 超出基線多少倍才視為「張口」
    private val kOverBaseline: Float = 1.5f,
    // 安全回落線（鬆手條件，避免一直黏在高分）
    private val releaseRatio: Float = 0.6f
) {
    private var ema: Float = 0f
    private var baseline: Float = 0.05f
    private var lastTs: Long = 0L

    private var aboveSince: Long? = null
    private var lastFireTs: Long = 0L
    private var latchedHigh = false

    data class Result(
        val yawnTriggered: Boolean,
        val scoreEma: Float,
        val baseline: Float,
        val threshold: Float
    )

    /**
     * @param rawOpenScore 通常取 max(jawOpen, mouthFunnel)
     * @param tsMs         時間戳（System.currentTimeMillis()）
     */
    fun update(rawOpenScore: Float, tsMs: Long): Result {
        if (lastTs == 0L) lastTs = tsMs

        // 低通濾波（EMA）：讓分數比較穩
        ema = if (ema == 0f) {
            rawOpenScore
        } else {
            alpha * rawOpenScore + (1f - alpha) * ema
        }

        // 動態基線：只在「相對低」的時候慢慢往下修 baseline
        if (ema < baseline * 1.3f) {
            baseline = baseline * (1f - baselineAlpha) + ema * baselineAlpha
        }

        val threshold = baseline * kOverBaseline
        val now = tsMs

        // 冷卻中：先不觸發，只看要不要把 latchedHigh 解鎖
        if (now - lastFireTs < cooldownMs) {
            if (latchedHigh && ema < threshold * releaseRatio) {
                latchedHigh = false
                aboveSince = null
            }
            Log.d(
                "YawnDetector",
                "cooldown raw=%.3f ema=%.3f base=%.3f th=%.3f"
                    .format(rawOpenScore, ema, baseline, threshold)
            )
            return Result(false, ema, baseline, threshold)
        }

        // 進入「高區域」
        if (ema >= threshold && !latchedHigh) {
            if (aboveSince == null) aboveSince = now
            // 高區域維持夠久 → 觸發一次
            if (now - (aboveSince ?: now) >= openHoldMs) {
                lastFireTs = now
                latchedHigh = true
                aboveSince = null
                Log.d(
                    "YawnDetector",
                    "TRIGGER raw=%.3f ema=%.3f base=%.3f th=%.3f"
                        .format(rawOpenScore, ema, baseline, threshold)
                )
                return Result(true, ema, baseline, threshold)
            }
        } else {
            // 低於門檻 或 已鎖定 → 檢查是否要解鎖 / 重置 aboveSince
            if (latchedHigh && ema < threshold * releaseRatio) {
                latchedHigh = false
                aboveSince = null
            }
            if (ema < threshold) {
                aboveSince = null
            }
        }

        Log.d(
            "YawnDetector",
            "noTrigger raw=%.3f ema=%.3f base=%.3f th=%.3f"
                .format(rawOpenScore, ema, baseline, threshold)
        )
        return Result(false, ema, baseline, threshold)
    }

    fun reset() {
        ema = 0f
        baseline = 0.05f
        aboveSince = null
        lastFireTs = 0L
        latchedHigh = false
        lastTs = 0L
    }
}
