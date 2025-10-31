package com.patrick.ui.fatigue

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.patrick.core.FatigueLevel
import com.patrick.ui.common.DrowsyGuardTopAppBar
import com.patrick.ui.common.getFatigueLevelTextColor

/**
 * 疲勞狀態顯示欄組件
 *
 * @param fatigueLevel 疲勞等級
 * @param isCalibrating 是否正在校正
 * @param onMenuClick 菜單按鈕點擊事件
 */
@Composable
fun FatigueStatusBar(
    fatigueLevel: FatigueLevel,
    isCalibrating: Boolean,
    onMenuClick: () -> Unit,
) {
    // 根據狀態決定標題文字
    val statusText =
        when {
            isCalibrating -> "正在校正中..."
            fatigueLevel == FatigueLevel.NORMAL -> "持續偵測中…"
            fatigueLevel == FatigueLevel.NOTICE -> "提醒：偵測到疲勞行為"
            fatigueLevel == FatigueLevel.WARNING -> "警告：請確認您的狀態"
            else -> "DrowsyGuard"
        }

    // 根據狀態決定標題顏色
    val titleColor =
        when {
            isCalibrating -> MaterialTheme.colorScheme.primary
            fatigueLevel == FatigueLevel.NORMAL -> MaterialTheme.colorScheme.onSurface
            else -> getFatigueLevelTextColor(fatigueLevel)
        }

    DrowsyGuardTopAppBar(
        title = statusText,
        titleColor = titleColor,
        onMenuClick = onMenuClick,
    )
}
