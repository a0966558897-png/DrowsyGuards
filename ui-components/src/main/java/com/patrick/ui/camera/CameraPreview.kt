package com.patrick.ui.camera

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 相機預覽組件
 *
 * @param previewView 相機預覽視圖
 * @param modifier 修飾符
 */
@Composable
fun CameraPreview(
    previewView: PreviewView,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )
}
