package com.patrick.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * DrowsyGuard 統一的進度條組件
 *
 * @param progress 進度值 (0.0 - 1.0)
 * @param progressText 進度文字，可選
 * @param modifier 修飾符
 * @param progressColor 進度條顏色，默認使用主題主色調
 * @param trackColor 軌道顏色，默認使用主題表面變體色
 * @param showText 是否顯示進度文字，默認為 true
 */
@Composable
fun DrowsyGuardProgressBar(
    progress: Float,
    progressText: String? = null,
    modifier: Modifier = Modifier,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showText: Boolean = true,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showText && progressText != null) {
            Text(
                text = progressText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        LinearProgressIndicator(
            progress = progress,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            color = progressColor,
            trackColor = trackColor,
        )
    }
}

/**
 * DrowsyGuard 帶百分比的進度條組件
 *
 * @param progress 進度百分比 (0-100)
 * @param progressText 進度文字前綴，默認為 "進度"
 * @param modifier 修飾符
 * @param progressColor 進度條顏色
 * @param trackColor 軌道顏色
 */
@Composable
fun DrowsyGuardPercentageProgressBar(
    progress: Int,
    progressText: String = "進度",
    modifier: Modifier = Modifier,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    DrowsyGuardProgressBar(
        progress = progress / 100f,
        progressText = "$progressText $progress%",
        modifier = modifier,
        progressColor = progressColor,
        trackColor = trackColor,
    )
}

/**
 * DrowsyGuard 校準進度條組件
 *
 * @param progress 校準進度百分比 (0-100)
 * @param modifier 修飾符
 */
@Composable
fun DrowsyGuardCalibrationProgressBar(
    progress: Int,
    modifier: Modifier = Modifier,
) {
    DrowsyGuardPercentageProgressBar(
        progress = progress,
        progressText = "校正中",
        modifier = modifier,
        progressColor = DrowsyGuardColors.CalibrationProgress,
        trackColor = DrowsyGuardColors.CalibrationTrack,
    )
}
