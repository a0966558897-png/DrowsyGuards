package com.patrick.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * DrowsyGuard 統一的按鈕組件
 *
 * @param text 按鈕文字
 * @param onClick 點擊事件
 * @param modifier 修飾符
 * @param enabled 是否啟用，默認為 true
 * @param colors 按鈕顏色，默認使用主題主色調
 * @param contentPadding 內容內邊距
 */
@Composable
fun DrowsyGuardButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Color = MaterialTheme.colorScheme.primary,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 88.dp, minHeight = 36.dp),
        enabled = enabled,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = colors,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        contentPadding = contentPadding,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * DrowsyGuard 次要按鈕組件
 *
 * @param text 按鈕文字
 * @param onClick 點擊事件
 * @param modifier 修飾符
 * @param enabled 是否啟用，默認為 true
 * @param contentPadding 內容內邊距
 */
@Composable
fun DrowsyGuardSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
) {
    DrowsyGuardButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = MaterialTheme.colorScheme.secondary,
        contentPadding = contentPadding,
    )
}

/**
 * DrowsyGuard 危險按鈕組件
 *
 * @param text 按鈕文字
 * @param onClick 點擊事件
 * @param modifier 修飾符
 * @param enabled 是否啟用，默認為 true
 * @param contentPadding 內容內邊距
 */
@Composable
fun DrowsyGuardDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
) {
    DrowsyGuardButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = DrowsyGuardColors.Error,
        contentPadding = contentPadding,
    )
}
