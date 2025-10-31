package com.patrick.detection

/**
 * 打呵欠偵測：
 * - 來源：FaceLandmarker blendshapes 的 jawOpen / mouthFunnel 分數 (0..1)
 * - 低通濾波（EMA）+ 持續時間門檻（至少 openHoldMs 毫秒）+ 冷卻（cooldownMs）
 * - 支援「自動基線」：用最近 3~5 秒的低值來當 baseline，動態門檻更穩定
 */
class YawnDetector(
    // 低通濾波係數（越小越平滑）
    private val alpha: Float = 0.25f,
    // 需維持「張口」多久才算打呵欠（毫秒）
    private val openHoldMs: Long = 900L,
    // 事件觸發後的冷卻時間（毫秒）
    private val cooldownMs: Long = 2500L,
    // 基線緩慢追蹤的學習率
    private val baselineAlpha: Float = 0.02f,
    // 超出基線多少才視為「張口」（倍率）
    private val kOverBaseline: Float = 1.8f,
    // 安全回落線（鬆手條件，避免一直黏在高分）
    private val releaseRatio: Float = 0.7f
) {
    private var ema: Float = 0f
    private var baseline: Float = 0.1f
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
     * @param tsMs         影像時間戳（或 SystemClock.elapsedRealtime()）
     */
    fun update(rawOpenScore: Float, tsMs: Long): Result {
        if (lastTs == 0L) lastTs = tsMs

        // 低通濾波（EMA）
        ema = if (ema == 0f) rawOpenScore else (alpha * rawOpenScore + (1f - alpha) * ema)

        // 動態基線：只用「較低」的 ema 來往下修 baseline，避免被一次大張口拉高
        if (ema < baseline * 1.2f) {
            baseline = baseline * (1f - baselineAlpha) + ema * baselineAlpha
        }

        val threshold = baseline * kOverBaseline
        val now = tsMs

        // 冷卻中 → 直接回報（不會觸發）
        if (now - lastFireTs < cooldownMs) {
            // 釋放鎖定：當 ema 回到 baseline 附近才解除
            if (latchedHigh && ema < threshold * releaseRatio) {
                latchedHigh = false
                aboveSince = null
            }
            return Result(false, ema, baseline, threshold)
        }

        // 進入高區域
        if (ema >= threshold && !latchedHigh) {
            if (aboveSince == null) aboveSince = now
            // 高區域持續夠久 → 觸發一次
            if (now - (aboveSince ?: now) >= openHoldMs) {
                lastFireTs = now
                latchedHigh = true
                aboveSince = null
                return Result(true, ema, baseline, threshold)
            }
        } else {
            // 低於門檻或已鎖定 → 檢查是否釋放
            if (latchedHigh && ema < threshold * releaseRatio) {
                latchedHigh = false
                aboveSince = null
            }
            if (ema < threshold) {
                aboveSince = null
            }
        }

        return Result(false, ema, baseline, threshold)
    }

    fun reset() {
        ema = 0f
        baseline = 0.1f
        aboveSince = null
        lastFireTs = 0L
        latchedHigh = false
        lastTs = 0L
    }
}
