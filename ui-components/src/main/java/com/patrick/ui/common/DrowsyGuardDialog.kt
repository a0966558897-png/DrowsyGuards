package com.patrick.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * DrowsyGuard 統一的對話框組件
 *
 * @param title 標題
 * @param message 消息內容
 * @param onConfirm 確認按鈕點擊事件
 * @param onDismiss 取消按鈕點擊事件
 * @param confirmText 確認按鈕文字，默認為 "確定"
 * @param dismissText 取消按鈕文字，默認為 "取消"
 * @param titleColor 標題顏色
 * @param messageColor 消息顏色
 * @param showDismissButton 是否顯示取消按鈕，默認為 true
 */
@Composable
fun DrowsyGuardDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "確定",
    dismissText: String = "取消",
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    messageColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showDismissButton: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = titleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = messageColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            DrowsyGuardButton(
                text = confirmText,
                onClick = onConfirm,
            )
        },
        dismissButton = {
            if (showDismissButton) {
                DrowsyGuardSecondaryButton(
                    text = dismissText,
                    onClick = onDismiss,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    )
}

/**
 * DrowsyGuard 雙按鈕對話框組件
 *
 * @param title 標題
 * @param message 消息內容
 * @param primaryButtonText 主要按鈕文字
 * @param secondaryButtonText 次要按鈕文字
 * @param onPrimaryClick 主要按鈕點擊事件
 * @param onSecondaryClick 次要按鈕點擊事件
 * @param onDismiss 取消事件
 * @param titleColor 標題顏色
 * @param messageColor 消息顏色
 */
@Composable
fun DrowsyGuardTwoButtonDialog(
    title: String,
    message: String,
    primaryButtonText: String,
    secondaryButtonText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    onDismiss: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    messageColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = titleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = messageColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DrowsyGuardButton(
                    text = primaryButtonText,
                    onClick = onPrimaryClick,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                DrowsyGuardSecondaryButton(
                    text = secondaryButtonText,
                    onClick = onSecondaryClick,
                    modifier = Modifier.weight(1f),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    )
}
