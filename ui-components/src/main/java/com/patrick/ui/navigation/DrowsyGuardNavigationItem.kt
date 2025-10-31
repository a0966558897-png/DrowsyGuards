package com.patrick.ui.navigation

import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * DrowsyGuard 導航項目組件
 * 封裝 Material3 的 NavigationDrawerItem
 *
 * @param icon 圖標內容
 * @param label 標籤內容
 * @param selected 是否選中
 * @param onClick 點擊事件
 * @param modifier 修飾符
 * @param badge 徽章內容，可選
 * @param enabled 是否啟用，默認為 true
 */
@Composable
fun DrowsyGuardNavigationItem(
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: @Composable (() -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") enabled: Boolean = true,
) {
    NavigationDrawerItem(
        icon = icon,
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        badge = badge,
    )
}
