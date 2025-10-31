package com.patrick.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * DrowsyGuard 側邊導航抽屜組件
 *
 * @param drawerState 抽屜狀態
 * @param selectedItem 當前選中的項目索引
 * @param onItemClick 項目點擊事件
 * @param content 主要內容
 */
@Composable
fun DrowsyGuardDrawer(
    drawerState: DrawerState,
    selectedItem: Int,
    onItemClick: (Int) -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.padding(12.dp))
                Text(
                    "DrowsyGuard",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.padding(12.dp))

                // 導航項目
                DrowsyGuardNavigationItems(
                    selectedItem = selectedItem,
                    onItemClick = onItemClick,
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        },
    ) {
        content()
    }
}

/**
 * DrowsyGuard 導航項目列表
 *
 * @param selectedItem 當前選中的項目索引
 * @param onItemClick 項目點擊事件
 */
@Composable
private fun DrowsyGuardNavigationItems(
    selectedItem: Int,
    onItemClick: (Int) -> Unit,
) {
    val navigationItems =
        listOf(
            NavigationItem("📷", "疲勞偵測", 0),
            NavigationItem("📁", "歷史記錄", 1),
            NavigationItem("⚙️", "設定", 2),
            NavigationItem("👤", "帳號", 3),
        )

    navigationItems.forEach { item ->
        DrowsyGuardNavigationItem(
            icon = { Text(item.icon) },
            label = { Text(item.label) },
            selected = selectedItem == item.index,
            onClick = { onItemClick(item.index) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

/**
 * 導航項目數據類
 */
private data class NavigationItem(
    val icon: String,
    val label: String,
    val index: Int,
)
