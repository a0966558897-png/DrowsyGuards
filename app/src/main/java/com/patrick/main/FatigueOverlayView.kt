package com.patrick.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.patrick.core.Constants
import com.patrick.core.FatigueDetectionResult
import com.patrick.core.FatigueEvent
import com.patrick.core.FatigueLevel
import com.patrick.core.FontLoader

/**
 * 疲劳检测覆盖视图
 * 在摄像头预览上显示疲劳状态和警报信息
 */
class FatigueOverlayView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private var fatigueResult: FatigueDetectionResult? = null
        private var scaleFactor: Float = 1f
        private var imageWidth: Int = 1
        private var imageHeight: Int = 1

        // 画笔
        private val fatigueStatusPaint = Paint()
        private val alertBackgroundPaint = Paint()
        private val alertTextPaint = Paint()
        private val eventIndicatorPaint = Paint()

        // 疲劳状态显示区域
        private val alertRect = RectF()

        init {
            initPaints()
        }

        private fun initPaints() {
            // 載入字體
            val customFont = FontLoader.loadNotoSansCJKTC(context)

            // 疲劳状态画笔
            fatigueStatusPaint.apply {
                style = Paint.Style.FILL
                textSize = 40f
                isAntiAlias = true
                customFont?.let { typeface = it }
            }

            // 警报背景画笔
            alertBackgroundPaint.apply {
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            // 警报文本画笔
            alertTextPaint.apply {
                style = Paint.Style.FILL
                textSize = 50f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                customFont?.let { typeface = it }
            }

            // 事件指示器画笔
            eventIndicatorPaint.apply {
                style = Paint.Style.FILL
                isAntiAlias = true
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            fatigueResult?.let { result ->
                // 绘制疲劳状态指示器
                drawFatigueStatusIndicator(canvas, result)

                // 如果检测到疲劳，绘制警报
                if (result.isFatigueDetected) {
                    drawFatigueAlert(canvas, result)
                }

                // 绘制事件指示器
                drawEventIndicators(canvas, result.events)
            }
        }

        /**
         * 绘制疲劳状态指示器
         */
        private fun drawFatigueStatusIndicator(
            canvas: Canvas,
            result: FatigueDetectionResult,
        ) {
            val statusText =
                when (result.fatigueLevel) {
                    FatigueLevel.NORMAL -> "正常"
                    FatigueLevel.NOTICE -> "提醒"
                    FatigueLevel.WARNING -> "警告"
                }
            val statusColor =
                when (result.fatigueLevel) {
                    FatigueLevel.NORMAL -> Color.parseColor(Constants.Colors.FATIGUE_NORMAL)
                    FatigueLevel.NOTICE -> Color.parseColor(Constants.Colors.FATIGUE_MODERATE)
                    FatigueLevel.WARNING -> Color.parseColor(Constants.Colors.FATIGUE_SEVERE)
                }

            fatigueStatusPaint.color = statusColor

            // 计算状态显示位置（右上角）
            val statusX = width - 20f
            val statusY = 60f

            // 绘制状态文本
            canvas.drawText(statusText, statusX, statusY, fatigueStatusPaint)

            // 绘制疲劳事件计数
            val eventCountText = "事件: ${result.events.size}"
            canvas.drawText(eventCountText, statusX, statusY + 40f, fatigueStatusPaint)
        }

        /**
         * 绘制疲劳警报
         */
        private fun drawFatigueAlert(
            canvas: Canvas,
            result: FatigueDetectionResult,
        ) {
            val alertMessage =
                when (result.fatigueLevel) {
                    FatigueLevel.NOTICE -> "⚠️ 偵測到疲勞行為，請注意安全！"
                    FatigueLevel.WARNING -> "🚨 疲勞警告！請立即確認您的狀態！"
                    else -> ""
                }
            if (alertMessage.isEmpty()) return
            alertBackgroundPaint.color =
                when (result.fatigueLevel) {
                    FatigueLevel.NOTICE -> Color.parseColor(Constants.Colors.WARNING_BACKGROUND)
                    FatigueLevel.WARNING -> Color.RED
                    else -> Color.TRANSPARENT
                }
            alertTextPaint.color =
                when (result.fatigueLevel) {
                    FatigueLevel.NOTICE -> Color.BLACK
                    FatigueLevel.WARNING -> Color.WHITE
                    else -> Color.BLACK
                }

            // 计算警报显示区域（屏幕中央）
            val alertWidth = width * 0.9f
            val alertHeight = 120f
            val alertX = (width - alertWidth) / 2f
            val alertY = height * 0.3f

            alertRect.set(alertX, alertY, alertX + alertWidth, alertY + alertHeight)

            // 绘制圆角背景
            canvas.drawRoundRect(alertRect, 20f, 20f, alertBackgroundPaint)

            // 绘制警报文本
            val textX = alertX + alertWidth / 2f
            val textY = alertY + alertHeight / 2f + 15f
            canvas.drawText(alertMessage, textX, textY, alertTextPaint)
        }

        /**
         * 绘制事件指示器
         */
        private fun drawEventIndicators(
            canvas: Canvas,
            events: List<FatigueEvent>,
        ) {
            if (events.isEmpty()) return

            // 在屏幕左侧显示事件指示器
            var indicatorY = 100f
            val indicatorX = 20f
            val indicatorSize = 30f

            events.forEach { event ->
                val (color, text) =
                    when (event) {
                        is FatigueEvent.EyeClosure -> {
                            Color.RED to "👁"
                        }
                        is FatigueEvent.Yawn -> {
                            Color.parseColor(Constants.Colors.FATIGUE_MODERATE) to "😮"
                        }
                        is FatigueEvent.HighBlinkFrequency -> {
                            Color.YELLOW to "👀"
                        }
                    }

                eventIndicatorPaint.color = color

                // 绘制事件指示器圆圈
                canvas.drawCircle(
                    indicatorX + indicatorSize / 2,
                    indicatorY + indicatorSize / 2,
                    indicatorSize / 2,
                    eventIndicatorPaint,
                )

                // 绘制事件图标
                alertTextPaint.color = Color.BLACK
                alertTextPaint.textSize = 20f
                canvas.drawText(
                    text,
                    indicatorX + indicatorSize / 2,
                    indicatorY + indicatorSize / 2 + 7f,
                    alertTextPaint,
                )

                indicatorY += indicatorSize + 10f
            }
        }

        /**
         * 设置疲劳检测结果
         */
        fun setFatigueResult(result: FatigueDetectionResult) {
            fatigueResult = result
            invalidate()
        }

        /**
         * 清除显示
         */
        fun clear() {
            fatigueResult = null
            invalidate()
        }

        /**
         * 设置图像尺寸和缩放因子
         */
        fun setImageDimensions(
            width: Int,
            height: Int,
            scale: Float,
        ) {
            imageWidth = width
            imageHeight = height
            scaleFactor = scale
        }
    }
