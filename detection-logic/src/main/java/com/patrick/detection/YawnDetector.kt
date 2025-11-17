package com.patrick.detection

import android.util.Log

class YawnDetector(
    private val openHoldMs: Long = 700L,
    private val cooldownMs: Long = 2000L,

    // 基線追蹤速度
    private val baselineAlpha: Float = 0.02f,

    // 超過基線多少倍算張嘴
    private val kOverBaseline: Float = 1.35f,

    // 鬆手比例
    private val releaseRatio: Float = 0.7f
) {
    private var ema = 0f
    private var baseline = 0.25f       // raw MAR baseline（閉嘴時大約 0.25）
    private var lastTs = 0L

    private var aboveSince: Long? = null
    private var lastFireTs = 0L
    private var latchedHigh = false

    data class Result(
        val yawnTriggered: Boolean,
        val scoreEma: Float,
        val baseline: Float,
        val threshold: Float
    )

    fun update(raw: Float, tsMs: Long): Result {
        if (lastTs == 0L) lastTs = tsMs
        lastTs = tsMs

        // EMA
        if (ema == 0f) ema = raw
        else ema = 0.25f * raw + 0.75f * ema

        // baseline 緩慢下追（閉嘴時 MAR 大概 0.23~0.28）
        if (ema < baseline * 1.2f) {
            baseline = baseline * (1 - baselineAlpha) + ema * baselineAlpha
        }

        val threshold = baseline * kOverBaseline
        val now = tsMs

        // cooldown 期間不觸發
        if (now - lastFireTs < cooldownMs) {
            if (latchedHigh && ema < threshold * releaseRatio) {
                latchedHigh = false
                aboveSince = null
            }
            return Result(false, ema, baseline, threshold)
        }

        // 進入張嘴區域
        if (ema >= threshold && !latchedHigh) {
            if (aboveSince == null) aboveSince = now

            // 持續超過 openHoldMs → 打呵欠
            if (now - (aboveSince ?: now) >= openHoldMs) {
                lastFireTs = now
                latchedHigh = true
                aboveSince = null
                return Result(true, ema, baseline, threshold)
            }

        } else {
            // 回到小於門檻 → reset
            if (latchedHigh && ema < threshold * releaseRatio) {
                latchedHigh = false
                aboveSince = null
            }
            if (ema < threshold) aboveSince = null
        }

        return Result(false, ema, baseline, threshold)
    }

    fun reset() {
        ema = 0f
        baseline = 0.25f
        aboveSince = null
        lastFireTs = 0L
        latchedHigh = false
        lastTs = 0L
    }
}

