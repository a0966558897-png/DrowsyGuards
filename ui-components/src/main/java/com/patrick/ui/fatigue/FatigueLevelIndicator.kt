package com.patrick.ui.fatigue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.patrick.core.FatigueLevel
import com.patrick.ui.common.getFatigueLevelColor

/**
 * 疲勞等級指示器組件
 *
 * @param fatigueLevel 疲勞等級
 * @param modifier 修飾符
 * @param showText 是否顯示文字，默認為 true
 */
@Composable
fun FatigueLevelIndicator(
    fatigueLevel: FatigueLevel,
    modifier: Modifier = Modifier,
    showText: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 圓形指示器
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(getFatigueLevelColor(fatigueLevel)),
            contentAlignment = Alignment.Center,
        ) {
            // 可以在這裡添加圖標或文字
        }

        if (showText) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text =
                    when (fatigueLevel) {
                        FatigueLevel.NORMAL -> "正常"
                        FatigueLevel.NOTICE -> "提醒"
                        FatigueLevel.WARNING -> "警告"
                    },
                style = MaterialTheme.typography.labelSmall,
                color = getFatigueLevelColor(fatigueLevel),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * 大型疲勞等級指示器組件
 *
 * @param fatigueLevel 疲勞等級
 * @param modifier 修飾符
 */
@Composable
fun FatigueLevelLargeIndicator(
    fatigueLevel: FatigueLevel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 大型圓形指示器
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getFatigueLevelColor(fatigueLevel)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text =
                    when (fatigueLevel) {
                        FatigueLevel.NORMAL -> "✓"
                        FatigueLevel.NOTICE -> "!"
                        FatigueLevel.WARNING -> "⚠"
                    },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text =
                when (fatigueLevel) {
                    FatigueLevel.NORMAL -> "狀態正常"
                    FatigueLevel.NOTICE -> "疲勞提醒"
                    FatigueLevel.WARNING -> "疲勞警告"
                },
            style = MaterialTheme.typography.titleMedium,
            color = getFatigueLevelColor(fatigueLevel),
            textAlign = TextAlign.Center,
        )
    }
}
