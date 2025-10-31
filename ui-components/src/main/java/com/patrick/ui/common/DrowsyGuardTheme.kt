package com.patrick.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * DrowsyGuard 應用主題系統
 * 提供亮色和暗色主題支持
 */

/**
 * 亮色主題顏色方案
 */
private val LightColorScheme =
    lightColorScheme(
        primary = DrowsyGuardColors.Primary,
        onPrimary = DrowsyGuardColors.OnPrimary,
        primaryContainer = DrowsyGuardColors.PrimaryVariant,
        onPrimaryContainer = DrowsyGuardColors.OnPrimary,
        secondary = DrowsyGuardColors.Secondary,
        onSecondary = DrowsyGuardColors.OnSecondary,
        secondaryContainer = DrowsyGuardColors.Secondary.copy(alpha = 0.2f),
        onSecondaryContainer = DrowsyGuardColors.OnSecondary,
        background = DrowsyGuardColors.Background,
        onBackground = DrowsyGuardColors.OnBackground,
        surface = DrowsyGuardColors.Surface,
        onSurface = DrowsyGuardColors.OnSurface,
        surfaceVariant = DrowsyGuardColors.SurfaceVariant,
        onSurfaceVariant = DrowsyGuardColors.OnSurfaceVariant,
        error = DrowsyGuardColors.Error,
        onError = DrowsyGuardColors.OnPrimary,
        errorContainer = DrowsyGuardColors.Error.copy(alpha = 0.2f),
        onErrorContainer = DrowsyGuardColors.Error,
    )

/**
 * 暗色主題顏色方案
 */
private val DarkColorScheme =
    darkColorScheme(
        primary = DrowsyGuardColors.Primary,
        onPrimary = DrowsyGuardColors.OnPrimary,
        primaryContainer = DrowsyGuardColors.PrimaryVariant,
        onPrimaryContainer = DrowsyGuardColors.OnPrimary,
        secondary = DrowsyGuardColors.Secondary,
        onSecondary = DrowsyGuardColors.OnSecondary,
        secondaryContainer = DrowsyGuardColors.Secondary.copy(alpha = 0.2f),
        onSecondaryContainer = DrowsyGuardColors.OnSecondary,
        background = Color(0xFF121212),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF1E1E1E),
        onSurface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFF2D2D2D),
        onSurfaceVariant = Color(0xFFCCCCCC),
        error = DrowsyGuardColors.Error,
        onError = DrowsyGuardColors.OnPrimary,
        errorContainer = DrowsyGuardColors.Error.copy(alpha = 0.2f),
        onErrorContainer = DrowsyGuardColors.Error,
    )

/**
 * DrowsyGuard 主題
 *
 * @param darkTheme 是否使用暗色主題，默認跟隨系統設置
 * @param content 主題內容
 */
@Composable
fun DrowsyGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DrowsyGuardTypography,
        content = content,
    )
}

/**
 * 獲取疲勞等級對應的顏色
 *
 * @param fatigueLevel 疲勞等級
 * @return 對應的顏色
 */
@Composable
fun getFatigueLevelColor(fatigueLevel: com.patrick.core.FatigueLevel): Color {
    return when (fatigueLevel) {
        com.patrick.core.FatigueLevel.NORMAL -> DrowsyGuardColors.Normal
        com.patrick.core.FatigueLevel.NOTICE -> DrowsyGuardColors.Notice
        com.patrick.core.FatigueLevel.WARNING -> DrowsyGuardColors.Warning
    }
}

/**
 * 獲取疲勞等級對應的文字顏色
 *
 * @param fatigueLevel 疲勞等級
 * @return 對應的文字顏色
 */
@Composable
fun getFatigueLevelTextColor(fatigueLevel: com.patrick.core.FatigueLevel): Color {
    return when (fatigueLevel) {
        com.patrick.core.FatigueLevel.NORMAL -> MaterialTheme.colorScheme.onSurface
        com.patrick.core.FatigueLevel.NOTICE -> DrowsyGuardColors.Notice
        com.patrick.core.FatigueLevel.WARNING -> DrowsyGuardColors.Warning
    }
}
