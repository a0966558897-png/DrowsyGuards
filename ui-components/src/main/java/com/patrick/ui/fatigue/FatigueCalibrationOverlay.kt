package com.patrick.ui.fatigue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrick.ui.common.DrowsyGuardCalibrationProgressBar

/**
 * 疲勞校正進度覆蓋層組件
 *
 * @param progress 校正進度 (0-100)
 * @param modifier 修飾符
 */
@Composable
fun FatigueCalibrationOverlay(
    progress: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .padding(bottom = 120.dp) // 避免被底部導航擋住
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(24.dp),
        ) {
            Text(
                text = "校正中… $progress%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))
            DrowsyGuardCalibrationProgressBar(
                progress = progress,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
    }
}
