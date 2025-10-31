package com.patrick.ui.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

/**
 * DrowsyGuard 統一的頂部應用欄組件
 *
 * @param title 標題文字
 * @param titleColor 標題顏色，默認使用主題顏色
 * @param onMenuClick 菜單按鈕點擊事件
 * @param showMenuButton 是否顯示菜單按鈕，默認為 true
 * @param actions 右側操作按鈕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrowsyGuardTopAppBar(
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onMenuClick: (() -> Unit)? = null,
    showMenuButton: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (showMenuButton && onMenuClick != null) {
                IconButton(onClick = onMenuClick) {
                    Text(
                        text = "☰",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        },
        actions = actions,
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                titleContentColor = titleColor,
            ),
    )
}
