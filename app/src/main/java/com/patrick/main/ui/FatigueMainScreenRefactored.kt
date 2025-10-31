package com.patrick.main.ui

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.patrick.core.FatigueLevel
import com.patrick.ui.fatigue.FatigueViewModel
import com.patrick.ui.fatigue.FatigueAlertDialog
import com.patrick.ui.fatigue.FatigueCalibrationOverlay
import com.patrick.ui.fatigue.FatigueStatusBar
import com.patrick.ui.navigation.DrowsyGuardDrawer
import com.patrick.ui.camera.CameraPreview


import kotlinx.coroutines.launch

/**
 * 重構後的疲勞主畫面
 * 使用新的 UI 組件庫，代碼更簡潔、更易維護
 *
 * @param fatigueLevel 疲勞等級
 * @param calibrationProgress 校正進度
 * @param isCalibrating 是否正在校正
 * @param showFatigueDialog 是否顯示疲勞對話框
 * @param previewView 相機預覽視圖
 * @param onUserAcknowledged 用戶確認事件
 * @param onUserRequestedRest 用戶要求休息事件
 */
@Composable
fun FatigueMainScreenRefactored(
    fatigueLevel: FatigueLevel,
    calibrationProgress: Int,
    isCalibrating: Boolean,
    showFatigueDialog: Boolean,
    previewView: PreviewView,
    onUserAcknowledged: () -> Unit = {},
    onUserRequestedRest: () -> Unit = {},
) {
    val drawerState = remember { androidx.compose.material3.DrawerState(DrawerValue.Closed) }
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf(0) }

    DrowsyGuardDrawer(
        drawerState = drawerState,
        selectedItem = selectedItem,
        onItemClick = { index ->
            selectedItem = index
            scope.launch { drawerState.close() }
        },
    ) {
        Scaffold(
            topBar = {
                FatigueStatusBar(
                    fatigueLevel = fatigueLevel,
                    isCalibrating = isCalibrating,
                    onMenuClick = { scope.launch { drawerState.open() } },
                )
            },
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                // 相機預覽
                CameraPreview(previewView = previewView)

                // 校正進度覆蓋層
                if (isCalibrating) {
                    FatigueCalibrationOverlay(progress = calibrationProgress)
                }

                // 疲勞警報對話框
                if (showFatigueDialog) {
                    FatigueAlertDialog(
                        fatigueLevel = fatigueLevel,
                        onAcknowledged = onUserAcknowledged,
                        onRequestRest = onUserRequestedRest,
                    )
                }
            }
        }
    }
}
