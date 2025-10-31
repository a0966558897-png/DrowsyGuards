package com.patrick.main.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.patrick.core.FatigueLevel
import kotlinx.coroutines.flow.SharedFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FatigueMainScreen(
    fatigueLevel: FatigueLevel,
    calibrationProgress: Int,
    isCalibrating: Boolean,
    showFatigueDialog: Boolean,
    previewView: PreviewView,
    statusText: String = "持續偵測中…",
    onUserAcknowledged: () -> Unit = {},
    onUserRequestedRest: () -> Unit = {},
    uiEvent: SharedFlow<Any>? = null,

    // 額外數據
    blinkFrequency: Int = 0,
    yawnCount: Int = 0,
    eyeClosureDuration: Long = 0L,

    // 中央「加分器」分數（0~100）
    fatigueScore: Int = 0,

    // 是否顯示這個 Composable 自己的 TopBar（預設 false）
    showTopBar: Boolean = false,

    // 底部「校正」按鈕 callback
    onRecalibrate: () -> Unit = {}
) {
    val rememberedPreview = remember(previewView) { previewView }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleMedium,
                            color = getStatusTextColor(fatigueLevel, isCalibrating),
                        )
                    }
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 相機預覽
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AndroidView(
                        factory = { rememberedPreview },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // 底部統計 + 四顆按鈕（最左：校正）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    FatigueScoreIndicator(score = fatigueScore)
                    Spacer(modifier = Modifier.height(16.dp))

                    DetectionStats(
                        blinkFrequency = blinkFrequency,
                        yawnCount = yawnCount,
                        eyeClosureDuration = eyeClosureDuration,
                        isCalibrating = isCalibrating,
                        calibrationProgress = calibrationProgress,
                    )

                    val ctx = LocalContext.current

                    Spacer(modifier = Modifier.height(4.dp))
                    ExtraPagesBar(
                        onRecalibrateClick = onRecalibrate,
                        onHistoryClick = { openByNames(ctx, "HistoryActivity") }
                    )
                }
            }

            // 疲勞提醒橫幅（非 NORMAL 且非校正時）
            if (fatigueLevel != FatigueLevel.NORMAL && !isCalibrating) {
                FatigueAlertBanner(fatigueLevel)
            }

            // 彈窗
            if (showFatigueDialog) {
                LocalFatigueAlertDialog(
                    fatigueLevel = fatigueLevel,
                    onUserAcknowledged = onUserAcknowledged,
                    onUserRequestedRest = onUserRequestedRest,
                )
            }
        }
    }
}

/* -------------------------- 以下是底部區塊與公用 UI -------------------------- */

@Composable
private fun ExtraPagesBar(
    onRecalibrateClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
) {
    val ctx = LocalContext.current
    val shape = RoundedCornerShape(12.dp)
    val btnColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFFF2F4F7),                 // 淺灰底
        contentColor   = MaterialTheme.colorScheme.onSurface // 文字色
    )
    val labelStyle = MaterialTheme.typography.labelLarge  // 統一字級，避免被裁切

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 校正（固定橫向單行，避免裁切）
        Button(
            onClick = onRecalibrateClick,
            colors = btnColors,
            shape = shape,
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clip(shape)
                .border(BorderStroke(1.dp, Color(0xFFE5E7EB)), shape)
        ) {
            Text(
                "校正",
                style = labelStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 歷史
        Button(
            onClick = onHistoryClick,
            colors = btnColors,
            shape = shape,
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clip(shape)
                .border(BorderStroke(1.dp, Color(0xFFE5E7EB)), shape)
        ) {
            Text(
                "歷史",
                style = labelStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 圖表
        Button(
            onClick = { openByNames(ctx, "ChartActivity") },
            colors = btnColors,
            shape = shape,
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clip(shape)
                .border(BorderStroke(1.dp, Color(0xFFE5E7EB)), shape)
        ) {
            Text(
                "圖表",
                style = labelStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 設定
        Button(
            onClick = { openByNames(ctx, "SettingsActivity") },
            colors = btnColors,
            shape = shape,
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clip(shape)
                .border(BorderStroke(1.dp, Color(0xFFE5E7EB)), shape)
        ) {
            Text(
                "設定",
                style = labelStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun openByNames(ctx: Context, simpleName: String) {
    val candidates = listOf(
        "${ctx.packageName}.$simpleName",
        "com.example.drivesafe.$simpleName",
        "drivesafe.$simpleName",
        "com.patrick.main.$simpleName",
        "com.patrick.$simpleName",
        "com.patrick.main.ui.$simpleName",
    )
    for (fqcn in candidates) {
        try {
            val clazz = Class.forName(fqcn)
            ctx.startActivity(Intent(ctx, clazz))
            return
        } catch (_: ClassNotFoundException) {}
    }
    Toast.makeText(ctx, "找不到 $simpleName，請確認類別的 package", Toast.LENGTH_SHORT).show()
}

@Composable
private fun getStatusTextColor(
    fatigueLevel: FatigueLevel,
    isCalibrating: Boolean,
): Color = when {
    isCalibrating -> MaterialTheme.colorScheme.primary
    fatigueLevel == FatigueLevel.NORMAL -> MaterialTheme.colorScheme.onSurface
    fatigueLevel == FatigueLevel.NOTICE -> Color(0xFFFF9800)
    fatigueLevel == FatigueLevel.WARNING -> Color(0xFFF44336)
    else -> MaterialTheme.colorScheme.onSurface
}

@Composable
private fun FatigueScoreIndicator(score: Int) {
    val safeScore = score.coerceIn(0, 100)
    val color = when (safeScore) {
        in 0..30 -> Color(0xFF4CAF50)
        in 31..60 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    val label = when (safeScore) {
        in 0..30 -> "正常"
        in 31..60 -> "輕度疲勞"
        else -> "重度疲勞"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = safeScore.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun DetectionStats(
    blinkFrequency: Int,
    yawnCount: Int,
    eyeClosureDuration: Long,
    isCalibrating: Boolean = false,
    calibrationProgress: Int = 0,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        if (isCalibrating) {
            CalibrationStatItem(progress = calibrationProgress, label = "校正中")
        } else {
            StatItem(
                value = "${blinkFrequency}次",
                label = "眨眼/分鐘",
                valueColor = MaterialTheme.colorScheme.onSurface,
            )
        }

        StatItem(
            value = "${yawnCount}次",
            label = "哈欠/分鐘",
            valueColor = MaterialTheme.colorScheme.onSurface,
        )

        val seconds = eyeClosureDuration / 1000.0
        StatItem(
            value = String.format("%.1f秒", seconds),
            label = "閉眼時間",
            valueColor = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StatItem(value: String, label: String, valueColor: Color) {
    Box(modifier = Modifier.height(80.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CalibrationStatItem(progress: Int, label: String) {
    Box(modifier = Modifier.height(80.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(
                progress = (progress / 100f).coerceIn(0f, 1f),
                modifier = Modifier
                    .height(8.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${progress.coerceIn(0, 100)}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FatigueAlertBanner(fatigueLevel: FatigueLevel) {
    val backgroundColor = when (fatigueLevel) {
        FatigueLevel.NOTICE -> Color(0xFFFFA500).copy(alpha = 0.3f)
        FatigueLevel.WARNING -> Color(0xFFFF0000).copy(alpha = 0.4f)
        else -> Color.Transparent
    }
    val borderColor = when (fatigueLevel) {
        FatigueLevel.NOTICE -> Color(0xFFFFA500)
        FatigueLevel.WARNING -> Color(0xFFFF0000)
        else -> Color.Transparent
    }
    val alertText = when (fatigueLevel) {
        FatigueLevel.NOTICE -> "⚠️ 提醒：偵測到疲勞行為"
        FatigueLevel.WARNING -> "🚨 警告：請確認您的狀態"
        else -> ""
    }

    if (alertText.isNotEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp),
        ) {
            Text(
                text = alertText,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier
                    .background(borderColor.copy(alpha = 0.85f), shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun LocalFatigueAlertDialog(
    fatigueLevel: FatigueLevel,
    onUserAcknowledged: () -> Unit,
    onUserRequestedRest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* 禁止點外面關閉，交給按鈕 */ },
        title = {
            val color = when (fatigueLevel) {
                FatigueLevel.NOTICE -> Color(0xFFFF9800)
                FatigueLevel.WARNING -> Color(0xFFF44336)
                else -> MaterialTheme.colorScheme.onSurface
            }
            Text(
                text = when (fatigueLevel) {
                    FatigueLevel.NOTICE -> "疲勞提醒"
                    FatigueLevel.WARNING -> "疲勞警告"
                    else -> "疲勞偵測"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = color,
            )
        },
        text = {
            Text(
                text = when (fatigueLevel) {
                    FatigueLevel.NOTICE -> "系統偵測到您可能處於疲勞狀態，請注意安全！"
                    FatigueLevel.WARNING -> "系統偵測到您處於警告狀態，請立即確認！"
                    else -> "系統偵測中…"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(onClick = onUserAcknowledged) { Text("我已清醒") }
        },
        dismissButton = {
            Button(onClick = onUserRequestedRest) { Text("我會找地方休息") }
        },
    )
}
