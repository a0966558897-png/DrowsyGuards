package com.patrick.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrick.main.ui.FatigueMainScreen
import com.patrick.main.ui.FatigueScreenViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        com.patrick.core.FatigueDetectionLogger.setLogEnabled(
            sensitivity = false,
            trigger = true,
            calibration = false,
            event = true,
            reset = false,
        )

        setContent { MainApp() }
    }
}


/** 沒有登入了 → 只剩 CALIBRATION / DETECT / HISTORY / HISTORY_DETAIL */
private enum class AppStep { CALIBRATION, DETECT, HISTORY, HISTORY_DETAIL }

@Composable
fun MainApp() {
    val context = LocalContext.current

    val vm: FatigueScreenViewModel =
        viewModel(factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FatigueScreenViewModel(context.applicationContext as android.app.Application) as T
            }
        })

    // 相機權限
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasCameraPermission = granted
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var step by remember { mutableStateOf(AppStep.CALIBRATION) }   // 直接從校正開始
    var selectedRecord by remember { mutableStateOf<RunRecord?>(null) }

    if (!hasCameraPermission) {
        PermissionRequestScreen { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }
        return
    }

    when (step) {
        AppStep.CALIBRATION -> CalibrationScreen(
            vm = vm,
            onCalibrationFinished = { step = AppStep.DETECT },
            onExit = { step = AppStep.CALIBRATION } // 無登入，就回校正
        )
        AppStep.DETECT -> DetectScreen(
            vm = vm,
            onEndRun = { step = AppStep.HISTORY },              // 仍可導到舊歷史（保留）
            onBackToCalibration = { step = AppStep.CALIBRATION } // 回校正
        )
        AppStep.HISTORY -> HistoryScreen(
            onBack = { step = AppStep.CALIBRATION }, // 返回 → 校正
            onOpen = { rec -> selectedRecord = rec; step = AppStep.HISTORY_DETAIL }
        )
        AppStep.HISTORY_DETAIL -> HistoryDetailScreen(
            record = requireNotNull(selectedRecord),
            onBack = { step = AppStep.HISTORY }
        )
    }
}

/* -------------------- Composables -------------------- */

@Composable
private fun CalibrationScreen(
    vm: FatigueScreenViewModel,
    onCalibrationFinished: () -> Unit,
    onExit: () -> Unit,
) {
    val appContext = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    val isCalibrating by vm.isCalibrating.collectAsState()
    val progress by vm.calibrationProgress.collectAsState()
    val status by vm.statusText.collectAsState()
    val previewView = remember { PreviewView(appContext) }

    LaunchedEffect(previewView, lifecycleOwner) {
        vm.initializeFatigueDetection(previewView, lifecycleOwner)
        vm.startCalibration()
    }

    LaunchedEffect(isCalibrating, progress) {
        if (!isCalibrating && progress >= 100) onCalibrationFinished()
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "校正中", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(text = status)

            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = (progress / 100f).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("${progress.coerceIn(0, 100)}%")

            Spacer(Modifier.height(24.dp))
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
                Text("重新開始校正")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetectScreen(
    vm: FatigueScreenViewModel,
    onEndRun: () -> Unit,
    onBackToCalibration: () -> Unit,          // 回校正
) {
    val appContext = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val fatigueLevel by vm.fatigueLevel.collectAsState()
    val calibrationProgress by vm.calibrationProgress.collectAsState()
    val isCalibrating by vm.isCalibrating.collectAsState()
    val showFatigueDialog by vm.showFatigueDialog.collectAsState()
    val blinkFrequency by vm.blinkFrequency.collectAsState()
    val yawnCount by vm.yawnCount.collectAsState()
    val eyeClosureDuration by vm.eyeClosureDuration.collectAsState() // ms
    val fatigueScore by vm.fatigueScore.collectAsState()

    val previewView = remember { PreviewView(appContext) }
    val startedAt = remember { System.currentTimeMillis() }

    // 偵測「疲勞提示彈窗」出現次數
    var alertCount by remember { mutableStateOf(0) }
    var lastDialog by remember { mutableStateOf(false) }
    LaunchedEffect(showFatigueDialog) {
        if (showFatigueDialog && !lastDialog) alertCount++
        lastDialog = showFatigueDialog
    }

    LaunchedEffect(previewView, lifecycleOwner) {
        vm.initializeFatigueDetection(previewView, lifecycleOwner)
        vm.startDetection()
    }

    fun endRunAndSave() {
        scope.launch {
            vm.stopDetection()
            RunStore.saveRun(
                context = appContext,
                record = RunRecord(
                    startedAt     = startedAt,
                    endedAt       = System.currentTimeMillis(),
                    avgScore      = fatigueScore,
                    blinkPerMin   = blinkFrequency.toFloat(),
                    yawnCount     = yawnCount,
                    eyeClosureSec = eyeClosureDuration / 1000f,
                    alertCount    = alertCount
                )
            )
            onEndRun() // 仍可導向舊歷史頁（保留）
        }
    }

    // ★ Drawer 與漢堡按鈕已移除，畫面更簡潔
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("持續偵測中...") },
                actions = {
                    TextButton(onClick = { endRunAndSave() }) { Text("結束此路程") }
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            FatigueMainScreen(
                fatigueLevel = fatigueLevel,
                calibrationProgress = calibrationProgress,
                isCalibrating = isCalibrating,
                showFatigueDialog = showFatigueDialog,
                previewView = previewView,
                statusText = "", // 隱藏中間提示條
                onUserAcknowledged = { vm.onUserAcknowledged() },
                onUserRequestedRest = { vm.onUserRequestedRest() },
                uiEvent = vm.uiEvent,
                fatigueScore = fatigueScore,
                blinkFrequency = blinkFrequency,
                yawnCount = yawnCount,
                eyeClosureDuration = eyeClosureDuration,
                // 底部最左邊「校正」
                onRecalibrate = {
                    vm.stopDetection()
                    onBackToCalibration()
                }
            )
        }
    }
}

/* 歷史紀錄頁面（讀取本機 RunStore） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreen(
    onBack: () -> Unit,
    onOpen: (RunRecord) -> Unit
) {
    val context = LocalContext.current
    var records by remember { mutableStateOf(RunStore.loadAll(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("歷史紀錄") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("〈 返回校正") }
                },
                actions = {
                    TextButton(onClick = {
                        RunStore.clear(context)
                        records = emptyList()
                    }) { Text("清除全部") }
                }
            )
        }
    ) { innerPadding ->
        if (records.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("目前沒有任何紀錄")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records) { r ->
                    HistoryItem(r, onClick = { onOpen(r) })
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(r: RunRecord, onClick: () -> Unit) {
    val fmt = remember { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()) }
    val durSec = ((r.endedAt - r.startedAt) / 1000).coerceAtLeast(0)
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("開始：${fmt.format(Date(r.startedAt))}")
            Text("結束：${fmt.format(Date(r.endedAt))}")
            Text("時長：${durSec}s")
            Spacer(Modifier.height(8.dp))
            Text("疲勞觸發：${r.alertCount} 次")
            Text("疲勞分數：${r.avgScore}")
            Text("眨眼頻率：${"%.2f".format(r.blinkPerMin)} /分")
            Text("哈欠次數：${r.yawnCount}")
            Text("閉眼時間：${"%.2f".format(r.eyeClosureSec)} 秒")
        }
    }
}

/* 歷史詳細頁 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryDetailScreen(
    record: RunRecord,
    onBack: () -> Unit
) {
    val fmt = remember { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()) }
    val durSec = ((record.endedAt - record.startedAt) / 1000).coerceAtLeast(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("紀錄明細") },
                navigationIcon = { TextButton(onClick = onBack) { Text("〈 返回") } }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("開始時間：${fmt.format(Date(record.startedAt))}")
            Text("結束時間：${fmt.format(Date(record.endedAt))}")
            Text("行駛時長：$durSec 秒")
            Divider()
            Text("疲勞觸發：${record.alertCount} 次")
            Text("疲勞分數：${record.avgScore}")
            Text("眨眼頻率：${"%.2f".format(record.blinkPerMin)} /分")
            Text("哈欠次數：${record.yawnCount}")
            Text("閉眼總時間：${"%.2f".format(record.eyeClosureSec)} 秒")
        }
    }
}

/* 權限畫面 */
@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp),
            ) {
                Text(text = "需要相機權限", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text(text = "為了進行疲勞偵測，此應用程式需要存取您的相機。")
                Spacer(Modifier.height(32.dp))
                Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
                    Text("授權相機權限")
                }
            }
        }
    }
}

/* -------------------- 輕量本機儲存（不用登入） -------------------- */

data class RunRecord(
    val startedAt: Long,
    val endedAt: Long,
    val avgScore: Int,
    val blinkPerMin: Float,
    val yawnCount: Int,
    val eyeClosureSec: Float,
    val alertCount: Int
)

object RunStore {
    private const val PREF = "local_runs_pref"
    private const val KEY = "records"

    fun saveRun(context: android.content.Context, record: RunRecord) {
        val sp = context.getSharedPreferences(PREF, android.content.Context.MODE_PRIVATE)
        val arr = JSONArray(sp.getString(KEY, "[]") ?: "[]")
        arr.put(JSONObject().apply {
            put("startedAt", record.startedAt)
            put("endedAt", record.endedAt)
            put("avgScore", record.avgScore)
            put("blinkPerMin", record.blinkPerMin.toDouble())
            put("yawnCount", record.yawnCount)
            put("eyeClosureSec", record.eyeClosureSec.toDouble())
            put("alertCount", record.alertCount)
        })
        sp.edit().putString(KEY, arr.toString()).apply()
    }

    fun loadAll(context: android.content.Context): List<RunRecord> {
        val sp = context.getSharedPreferences(PREF, android.content.Context.MODE_PRIVATE)
        val arr = JSONArray(sp.getString(KEY, "[]") ?: "[]")
        val list = ArrayList<RunRecord>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                RunRecord(
                    startedAt = o.getLong("startedAt"),
                    endedAt = o.getLong("endedAt"),
                    avgScore = o.getInt("avgScore"),
                    blinkPerMin = o.getDouble("blinkPerMin").toFloat(),
                    yawnCount = o.getInt("yawnCount"),
                    eyeClosureSec = o.getDouble("eyeClosureSec").toFloat(),
                    alertCount = o.optInt("alertCount", 0)
                )
            )
        }
        return list.sortedByDescending { it.startedAt }
    }

    fun clear(context: android.content.Context) {
        val sp = context.getSharedPreferences(PREF, android.content.Context.MODE_PRIVATE)
        sp.edit().remove(KEY).apply()
    }
}
