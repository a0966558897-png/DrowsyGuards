package com.patrick.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 相機覆蓋層組件
 *
 * @param message 顯示的消息
 * @param backgroundColor 背景顏色，默認半透明黑色
 * @param textColor 文字顏色，默認白色
 * @param modifier 修飾符
 */
@Composable
fun CameraOverlay(
    message: String,
    backgroundColor: Color = Color(0x80000000),
    textColor: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.headlineMedium,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )
    }
}

/**
 * 相機權限請求覆蓋層組件
 *
 * @param onRequestPermission 請求權限事件
 * @param modifier 修飾符
 */
@Composable
fun CameraPermissionOverlay(
    @Suppress("UNUSED_PARAMETER") onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0x80000000)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "需要相機權限才能使用疲勞偵測功能",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )
    }
}
