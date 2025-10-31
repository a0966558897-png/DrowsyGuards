# DrowsyGuard 建置紀錄

## 📋 專案概述

DrowsyGuard 是一個基於 Android 的疲勞偵測應用程式，採用 Clean Architecture 架構設計，使用 CameraX 進行相機操作，MediaPipe 進行面部特徵點偵測，並透過 EAR (Eye Aspect Ratio) 和 MAR (Mouth Aspect Ratio) 演算法進行疲勞狀態分析。

## 🏗️ 模組架構

### 1. 目前模組與功能簡介

| 模組名稱 | 主要功能 | 核心職責 |
|---------|---------|---------|
| **app** | 主應用程式模組 | UI 層、Activity 管理、Compose UI 實現 |
| **camera-input** | 相機初始化與影像擷取 | CameraX 管理、預覽控制、影像分析 |
| **detection-logic** | 疲勞偵測邏輯 | MediaPipe 整合、EAR/MAR 計算、疲勞狀態判斷 |
| **user-alert** | 使用者警示系統 | 疲勞警告、對話框管理、震動/聲音提醒 |
| **shared-core** | 共享核心功能 | 常數定義、回調接口、工具類 |
| **user-settings** | 使用者設定 | 設定管理、偏好儲存 |
| **account-auth** | 帳號認證 | 使用者認證、登入管理 |

### 2. 模組依賴關係

```
app
├── camera-input
├── detection-logic
├── user-alert
├── shared-core
├── user-settings
└── account-auth

camera-input
├── shared-core
└── detection-logic

detection-logic
├── shared-core
└── user-alert

user-alert
└── shared-core
```

## 🔌 模組對外接口與使用方式

### 📱 App 模組

#### 主要類別
- **MainActivity**: 應用程式主入口點
- **FatigueMainScreen**: 主要 UI 畫面 (Compose)

#### 使用方式
```kotlin
// 在 MainActivity 中設置 Compose UI
setContent {
    CameraScreen()
}

// FatigueMainScreen 使用方式
FatigueMainScreen(
    fatigueLevel = fatigueLevel,
    calibrationProgress = calibrationProgress,
    isCalibrating = isCalibrating,
    showFatigueDialog = showFatigueDialog,
    previewView = previewView,
    onUserAcknowledged = { /* 處理使用者確認 */ },
    onUserRequestedRest = { /* 處理使用者要求休息 */ }
)
```

### 📷 Camera-Input 模組

#### 主要類別
- **CameraViewModel**: 相機狀態管理 ViewModel
- **CameraUseCase**: 相機業務邏輯用例
- **CameraRepository**: 相機資料存取抽象
- **CameraController**: 相機硬體控制
- **CameraManager**: 相機高級操作管理

#### 對外接口

##### CameraViewModel
```kotlin
class CameraViewModel(application: Application) : AndroidViewModel(application) {
    // 狀態流
    val fatigueLevel: StateFlow<FatigueLevel>
    val calibrationProgress: StateFlow<Int>
    val isCalibrating: StateFlow<Boolean>
    val showFatigueDialog: StateFlow<Boolean>
    
    // 主要方法
    fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner)
    fun releaseCamera()
    
    // FatigueUiCallback 實現
    override fun onCalibrationStarted()
    override fun onCalibrationProgress(progress: Int, currentEar: Float)
    override fun onCalibrationCompleted(newThreshold: Float, minEar: Float, maxEar: Float, avgEar: Float)
    override fun onModerateFatigue()
    override fun onUserAcknowledged()
    override fun onUserRequestedRest()
}
```

##### CameraUseCase
```kotlin
class CameraUseCase(private val repository: CameraRepository) {
    // 狀態查詢
    val cameraState: StateFlow<CameraRepository.CameraState>
    val errorMessage: StateFlow<String?>
    
    // 相機操作
    suspend fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner)
    suspend fun rebindCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner)
    suspend fun releaseCamera()
    
    // 回調設置
    fun setFaceLandmarksCallback(callback: (FaceLandmarkerResult) -> Unit)
    
    // 狀態檢查
    fun isCameraReady(): Boolean
    fun checkCameraStatus(): String
}
```

#### 使用方式
```kotlin
// 創建 CameraViewModel
val cameraViewModel: CameraViewModel = viewModel(
    factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CameraViewModel(context as Application) as T
        }
    }
)

// 初始化相機
cameraViewModel.initializeCamera(previewView, lifecycleOwner)

// 監聽狀態
val fatigueLevel by cameraViewModel.fatigueLevel.collectAsState()
val calibrationProgress by cameraViewModel.calibrationProgress.collectAsState()
```

### 🔍 Detection-Logic 模組

#### 主要類別
- **FatigueDetectionManager**: 疲勞偵測管理器
- **FatigueDetector**: 疲勞偵測核心邏輯
- **FaceLandmarkerManager**: MediaPipe 面部特徵點管理器

#### 對外接口

##### FatigueDetectionManager
```kotlin
class FatigueDetectionManager(
    private val context: Context,
    private val uiCallback: FatigueUiCallback
) : FatigueDetectionListener {
    
    // 主要方法
    fun processFaceLandmarks(result: FaceLandmarkerResult)
    fun startDetection()
    fun stopDetection()
    fun startCalibration()
    fun stopCalibration()
    
    // 狀態查詢
    fun isCalibrating(): Boolean
    fun getCalibrationProgress(): Int
    fun getCurrentFatigueLevel(): FatigueLevel
    fun getFatigueEventCount(): Int
    
    // 參數設置
    fun setDetectionParameters(
        earThreshold: Float? = null,
        marThreshold: Float? = null,
        fatigueEventThreshold: Int? = null
    )
    
    // 重置與清理
    fun reset()
    fun cleanup()
}
```

##### FatigueDetector
```kotlin
class FatigueDetector {
    // 主要方法
    fun processFaceLandmarks(result: FaceLandmarkerResult): FatigueDetectionResult
    fun startCalibration()
    fun stopCalibration()
    fun reset()
    
    // 參數設置
    fun setDetectionParameters(earThreshold: Float, marThreshold: Float, fatigueEventThreshold: Int)
    fun setFatigueListener(listener: FatigueDetectionListener)
    
    // 狀態查詢
    fun isCalibrating(): Boolean
    fun getCalibrationProgress(): Int
    fun getFatigueEventCount(): Int
    fun getRecentBlinkCount(windowMs: Long): Int
}
```

#### 使用方式
```kotlin
// 創建疲勞偵測管理器
val fatigueDetectionManager = FatigueDetectionManager(context, uiCallback)

// 啟動偵測
fatigueDetectionManager.startDetection()
fatigueDetectionManager.startCalibration()

// 處理面部特徵點
fatigueDetectionManager.processFaceLandmarks(faceLandmarkerResult)

// 監聽狀態
val isCalibrating = fatigueDetectionManager.isCalibrating()
val progress = fatigueDetectionManager.getCalibrationProgress()
```

### 🚨 User-Alert 模組

#### 主要類別
- **FatigueAlertManager**: 疲勞警示管理器
- **FatigueDialogManager**: 對話框管理器

#### 對外接口

##### FatigueAlertManager
```kotlin
class FatigueAlertManager(private val context: Context) {
    // 主要方法
    fun handleFatigueDetection(result: FatigueDetectionResult)
    fun stopAllAlerts()
    fun cleanup()
    
    // 回調設置
    fun setDialogCallback(callback: FatigueDialogCallback)
    fun setModerateFatigueCallback(callback: ModerateFatigueCallback)
    fun setUiCallback(callback: FatigueUiCallback)
    
    // 狀態管理
    fun onModerateFatigueCleared()
}
```

##### FatigueDialogManager
```kotlin
class FatigueDialogManager(private val context: Context) {
    // 對話框顯示
    fun showModerateFatigueDialog(activity: Activity)
    fun showSevereFatigueDialog(activity: Activity)
    fun showRestReminderDialog(activity: Activity)
    
    // 對話框關閉
    fun dismissAllDialogs()
}
```

#### 使用方式
```kotlin
// 創建警示管理器
val alertManager = FatigueAlertManager(context)

// 設置回調
alertManager.setDialogCallback(object : FatigueDialogCallback {
    override fun onUserAcknowledged() { /* 處理使用者確認 */ }
    override fun onUserRequestedRest() { /* 處理使用者要求休息 */ }
})

// 處理疲勞偵測結果
alertManager.handleFatigueDetection(fatigueResult)
```

### 🔧 Shared-Core 模組

#### 主要類別
- **Constants**: 常數定義
- **FatigueUiCallback**: UI 回調接口
- **FatigueModels**: 疲勞偵測資料模型
- **PermissionManager**: 權限管理
- **FontLoader**: 字體載入器

#### 對外接口

##### Constants
```kotlin
object Constants {
    object FatigueDetection {
        const val DEFAULT_EAR_THRESHOLD = 0.21f
        const val DEFAULT_MAR_THRESHOLD = 0.6f
        const val FATIGUE_EVENT_THRESHOLD = 3
        const val CALIBRATION_DURATION_MS = 10000L
        const val BLINK_WINDOW_MS = 5000L
    }
    
    object Camera {
        const val TARGET_RESOLUTION_WIDTH = 640
        const val TARGET_RESOLUTION_HEIGHT = 480
        const val ANALYSIS_INTERVAL_MS = 100L
    }
}
```

##### FatigueUiCallback
```kotlin
interface FatigueUiCallback {
    fun onBlink()
    fun onCalibrationStarted()
    fun onCalibrationProgress(progress: Int, currentEar: Float)
    fun onCalibrationCompleted(newThreshold: Float, minEar: Float, maxEar: Float, avgEar: Float)
    fun onModerateFatigue()
    fun onUserAcknowledged()
    fun onUserRequestedRest()
    fun onFatigueAlert(message: String)
}
```

##### FatigueModels
```kotlin
enum class FatigueLevel {
    NORMAL, MODERATE, SEVERE
}

data class FatigueDetectionResult(
    val fatigueLevel: FatigueLevel,
    val isFatigueDetected: Boolean,
    val events: List<FatigueEvent>,
    val earValue: Float,
    val marValue: Float
)

data class FatigueEvent(
    val type: FatigueEventType,
    val timestamp: Long,
    val severity: Float
)
```

#### 使用方式
```kotlin
// 使用常數
val earThreshold = Constants.FatigueDetection.DEFAULT_EAR_THRESHOLD
val targetWidth = Constants.Camera.TARGET_RESOLUTION_WIDTH

// 實現回調接口
class MyFatigueCallback : FatigueUiCallback {
    override fun onCalibrationStarted() {
        // 處理校正開始
    }
    
    override fun onModerateFatigue() {
        // 處理中度疲勞
    }
    // ... 其他方法實現
}

// 使用資料模型
val result = FatigueDetectionResult(
    fatigueLevel = FatigueLevel.MODERATE,
    isFatigueDetected = true,
    events = listOf(),
    earValue = 0.18f,
    marValue = 0.65f
)
```

## 🛠️ 工具類與單例

### CameraModule (單例)
```kotlin
object CameraModule {
    fun createCameraModule(context: Context): CameraUseCase {
        val repository = CameraRepositoryImpl(context)
        return CameraUseCase(repository)
    }
}
```

### FaceLandmarkerManager (工具類)
```kotlin
object FaceLandmarkerManager {
    fun createForRealTime(context: Context): FaceLandmarker {
        // 創建支援實時處理的 FaceLandmarker
    }
}
```

### ImageUtils (工具類)
```kotlin
object ImageUtils {
    fun ImageProxy.toBitmap(): Bitmap {
        // 將 ImageProxy 轉換為 Bitmap
    }
}
```

## 📦 版本管理

專案使用 Gradle Version Catalogs (`gradle/libs.versions.toml`) 統一管理所有依賴版本：

```toml
[versions]
camerax = "1.3.1"
mediapipe = "0.10.8"
compose = "2024.02.00"
accompanist = "0.32.0"

[libraries]
androidx-camera-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
androidx-camera-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
androidx-camera-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
androidx-camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }
mediapipe-tasks-vision = { group = "com.google.mediapipe", name = "tasks-vision", version.ref = "mediapipe" }
```

## 🚀 建置與部署

### 建置指令
```bash
# 清理並重新建置
./gradlew clean build

# 建置 Debug 版本
./gradlew assembleDebug

# 建置 Release 版本
./gradlew assembleRelease

# 執行測試
./gradlew test
```

### 部署需求
- Android API Level 21+ (Android 5.0+)
- Camera 權限
- 前置相機支援
- 至少 2GB RAM
- 支援 OpenGL ES 2.0

## 📝 注意事項

1. **權限管理**: 應用程式需要相機權限才能正常運作
2. **效能優化**: 使用 STRATEGY_KEEP_ONLY_LATEST 避免記憶體洩漏
3. **生命週期管理**: 所有相機操作都綁定到 LifecycleOwner
4. **錯誤處理**: 包含完整的錯誤處理和重試機制
5. **模組化設計**: 各模組間透過接口進行解耦

---

*最後更新: 2024-07-14*
*版本: 1.0.0* 