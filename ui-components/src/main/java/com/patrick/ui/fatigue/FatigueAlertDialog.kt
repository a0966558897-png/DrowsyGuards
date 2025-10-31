package com.patrick.ui.fatigue

import androidx.compose.runtime.Composable
import com.patrick.core.FatigueLevel
import com.patrick.ui.common.DrowsyGuardTwoButtonDialog
import com.patrick.ui.common.getFatigueLevelTextColor

/**
 * 疲勞警報對話框組件
 *
 * @param fatigueLevel 疲勞等級
 * @param onAcknowledged 用戶確認事件
 * @param onRequestRest 用戶要求休息事件
 * @param onDismiss 取消事件
 */
@Composable
fun FatigueAlertDialog(
    fatigueLevel: FatigueLevel,
    onAcknowledged: () -> Unit,
    onRequestRest: () -> Unit,
    onDismiss: () -> Unit = {},
) {
    val title =
        when (fatigueLevel) {
            FatigueLevel.NOTICE -> "疲勞提醒"
            FatigueLevel.WARNING -> "疲勞警告"
            else -> "疲勞偵測"
        }

    val message =
        when (fatigueLevel) {
            FatigueLevel.NOTICE -> "系統偵測到您可能處於疲勞狀態，請注意安全！"
            FatigueLevel.WARNING -> "系統偵測到您處於警告狀態，請立即確認！"
            else -> "系統偵測中…"
        }

    val primaryButtonText = "我已清醒"
    val secondaryButtonText = "我會找地方休息"

    DrowsyGuardTwoButtonDialog(
        title = title,
        message = message,
        primaryButtonText = primaryButtonText,
        secondaryButtonText = secondaryButtonText,
        onPrimaryClick = onAcknowledged,
        onSecondaryClick = onRequestRest,
        onDismiss = onDismiss,
        titleColor = getFatigueLevelTextColor(fatigueLevel),
    )
}
