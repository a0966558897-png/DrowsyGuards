package com.patrick.ui.common

import androidx.compose.ui.graphics.Color

/**
 * DrowsyGuard 應用顏色系統
 * 定義應用中使用的所有顏色常量
 */
object DrowsyGuardColors {
    // 主要顏色
    val Primary = Color(0xFF2196F3) // 藍色主色調
    val PrimaryVariant = Color(0xFF1976D2) // 深藍色變體
    val Secondary = Color(0xFF03DAC6) // 青色輔助色

    // 疲勞等級顏色
    val Normal = Color(0xFF4CAF50) // 正常狀態 - 綠色
    val Notice = Color(0xFFFF9800) // 提醒狀態 - 橙色
    val Warning = Color(0xFFF44336) // 警告狀態 - 紅色

    // 背景顏色
    val Background = Color(0xFFFAFAFA) // 主背景色
    val Surface = Color(0xFFFFFFFF) // 表面背景色
    val SurfaceVariant = Color(0xFFF5F5F5) // 表面變體色

    // 文字顏色
    val OnPrimary = Color(0xFFFFFFFF) // 主色調上的文字
    val OnSecondary = Color(0xFF000000) // 輔助色上的文字
    val OnBackground = Color(0xFF000000) // 背景上的文字
    val OnSurface = Color(0xFF000000) // 表面上的文字
    val OnSurfaceVariant = Color(0xFF666666) // 表面變體上的文字

    // 狀態顏色
    val Success = Color(0xFF4CAF50) // 成功狀態
    val Error = Color(0xFFF44336) // 錯誤狀態
    val Info = Color(0xFF2196F3) // 信息狀態

    // 透明度顏色
    val Overlay = Color(0x80000000) // 半透明黑色覆蓋層
    val SemiTransparent = Color(0xCCFFFFFF) // 半透明白色

    // 校準進度顏色
    val CalibrationProgress = Color(0xFF2196F3) // 校準進度條顏色
    val CalibrationTrack = Color(0xFFE0E0E0) // 校準進度條軌道顏色
}
