# DrowsyGuard 專案規範文檔

## 📋 目錄
1. [專案概述](#專案概述)
2. [命名規範](#命名規範)
3. [項目結構規範](#項目結構規範)
4. [模組設計規範](#模組設計規範)
5. [註釋方式規範](#註釋方式規範)
6. [代碼風格規範](#代碼風格規範)
7. [架構設計規範](#架構設計規範)
8. [依賴管理規範](#依賴管理規範)
9. [測試規範](#測試規範)
10. [文檔規範](#文檔規範)

---

## 🎯 專案概述

DrowsyGuard 是一個基於 MediaPipe 的 Android 疲勞偵測應用，採用 Clean Architecture 設計模式，使用 Kotlin 和 Jetpack Compose 開發。

### 技術棧
- **語言**: Kotlin
- **UI 框架**: Jetpack Compose
- **架構模式**: Clean Architecture
- **依賴注入**: 手動依賴注入
- **狀態管理**: StateFlow
- **相機**: CameraX
- **AI 模型**: MediaPipe Face Landmarker
- **構建工具**: Gradle (Version Catalog)

---

## 📝 命名規範

### 1. **包命名規範**
```kotlin
// 格式: com.patrick.{module}.{subpackage}
package com.patrick.main
package com.patrick.camera
package com.patrick.detection
package com.patrick.alert
package com.patrick.core
```

### 2. **類命名規範**
```kotlin
// 使用 PascalCase
class MainActivity
class FatigueDetector
class CameraController
class FatigueAlertManager

// 介面使用 PascalCase，不加前綴
interface FatigueUiCallback
interface CameraRepository

// 枚舉使用 PascalCase
enum class FatigueLevel {
    NORMAL, NOTICE, WARNING
}

// 數據類使用 PascalCase
data class FatigueDetectionResult(
    val isFatigueDetected: Boolean,
    val fatigueLevel: FatigueLevel,
    val events: List<FatigueEvent>
)
```

### 3. **函數命名規範**
```kotlin
// 使用 camelCase
fun processFaceLandmarks(result: FaceLandmarkerResult): FatigueDetectionResult
fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner)
fun onUserAcknowledged()
fun triggerVibration()

// 私有函數使用 camelCase
private fun detectEyeClosure(landmarks: List<NormalizedLandmark>, currentTime: Long): FatigueEvent?
private fun calculateEAR(landmarks: List<NormalizedLandmark>, eyeIndices: List<Int>): Float
```

### 4. **變數命名規範**
```kotlin
// 使用 camelCase
private var currentEarThreshold = DEFAULT_EAR_THRESHOLD
private var isEyeClosed = false
private val blinkTimestamps = mutableListOf<Long>()

// 常量使用 UPPER_SNAKE_CASE
const val DEFAULT_EAR_THRESHOLD = 0.20f
const val ALERT_DURATION_MS = 3000L
const val TAG = "FatigueDetector"

// 伴生對象常量
companion object {
    private const val TAG = "FatigueDetector"
    const val DEFAULT_EAR_THRESHOLD = 0.20f
}
```

### 5. **資源命名規範**
```xml
<!-- 佈局文件 -->
activity_main.xml
fragment_camera.xml
item_fatigue_event.xml

<!-- 顏色資源 -->
colors.xml
<color name="fatigue_normal">#4CAF50</color>
<color name="fatigue_notice">#FF9800</color>
<color name="fatigue_warning">#F44336</color>

<!-- 字符串資源 -->
strings.xml
<string name="app_name">DrowsyGuard</string>
<string name="fatigue_detection">疲勞偵測</string>
```

---

## 📁 項目結構規範

### 1. **根目錄結構**
```
DrowsyGuard/
├── app/                    # 主應用模組
├── camera-input/           # 相機輸入模組
├── detection-logic/        # 偵測邏輯模組
├── user-alert/            # 用戶警報模組
├── user-settings/         # 用戶設定模組
├── account-auth/          # 帳號認證模組
├── shared-core/           # 共享核心模組
├── gradle/                # Gradle 配置
├── build.gradle           # 根項目構建文件
├── settings.gradle        # 項目設置
└── README.md              # 項目說明
```

### 2. **模組內部結構**
```
{module}/
├── build.gradle           # 模組構建文件
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/patrick/{module}/
│   │   │   ├── {MainClass}.kt
│   │   │   ├── {Repository}.kt
│   │   │   ├── {UseCase}.kt
│   │   │   └── {Manager}.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── values/
│   │   │   ├── drawable/
│   │   │   └── raw/
│   │   └── assets/
│   ├── test/
│   │   └── java/com/patrick/{module}/
│   └── androidTest/
│       └── java/com/patrick/{module}/
└── proguard-rules.pro
```

### 3. **Java 包結構**
```kotlin
// 主要包結構
com.patrick.main          # 主應用
com.patrick.camera        # 相機功能
com.patrick.detection     # 偵測邏輯
com.patrick.alert         # 警報功能
com.patrick.settings      # 設定功能
com.patrick.auth          # 認證功能
com.patrick.core          # 核心功能

// 子包結構
com.patrick.main.ui       # UI 組件
com.patrick.main.fragment # Fragment
com.patrick.camera.repository # 相機數據層
com.patrick.camera.usecase    # 相機業務邏輯
```

---

## 🏗️ 模組設計規範

### 1. **模組職責分離**

#### **app 模組 (主應用)**
- 職責：UI 展示、用戶交互、應用入口
- 組件：MainActivity、Compose UI、ViewModel
- 依賴：所有其他模組

#### **camera-input 模組**
- 職責：相機操作、MediaPipe 整合
- 組件：CameraController、CameraManager、CameraRepository
- 依賴：shared-core

#### **detection-logic 模組**
- 職責：疲勞偵測算法、特徵計算
- 組件：FatigueDetector、FaceLandmarkerManager
- 依賴：shared-core

#### **user-alert 模組**
- 職責：警報管理、聲音震動、對話框
- 組件：FatigueAlertManager、FatigueDialogManager
- 依賴：shared-core

#### **shared-core 模組**
- 職責：共享數據模型、常數、工具類
- 組件：FatigueModels、Constants、FatigueUiCallback
- 依賴：無

### 2. **模組間依賴關係**
```
app
├── camera-input
├── detection-logic
├── user-alert
├── user-settings
├── account-auth
└── shared-core

camera-input ──→ shared-core
detection-logic ──→ shared-core
user-alert ──→ shared-core
user-settings ──→ shared-core
account-auth ──→ shared-core
```

### 3. **模組內部架構**
```kotlin
// 每個模組遵循 Clean Architecture
{module}/
├── Repository.kt          # 數據層接口
├── RepositoryImpl.kt      # 數據層實現
├── UseCase.kt             # 業務邏輯層
├── Manager.kt             # 管理層
└── Module.kt              # 依賴注入工廠
```

---

## 📖 註釋方式規範

### 1. **文件頭註釋**
```kotlin
/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.patrick.detection
```

### 2. **類註釋 (KDoc)**
```kotlin
/**
 * 疲劳检测器 - 核心疲劳检测逻辑
 * 基于MediaPipe面部特征点检测结果进行疲劳分析
 * 
 * @author Patrick
 * @since 1.0.0
 */
class FatigueDetector {
    // 實現...
}
```

### 3. **函數註釋 (KDoc)**
```kotlin
/**
 * 处理面部特征点检测结果
 * 
 * @param result MediaPipe 面部特徵點檢測結果
 * @return 疲勞檢測結果，包含疲勞等級和事件列表
 * 
 * @throws IllegalArgumentException 當輸入參數為空時
 * @see FatigueDetectionResult
 * @see FatigueLevel
 */
fun processFaceLandmarks(result: FaceLandmarkerResult): FatigueDetectionResult {
    // 實現...
}
```

### 4. **屬性註釋**
```kotlin
/**
 * EAR (Eye Aspect Ratio) 阈值 - 根據實際 EAR 值調整
 * 標準閾值：睜眼 0.28-0.35，閉眼 0.08-0.14，閾值 0.20
 */
const val DEFAULT_EAR_THRESHOLD = 0.20f

/**
 * 疲劳检测状态
 */
private var currentEarThreshold = DEFAULT_EAR_THRESHOLD
```

### 5. **行內註釋**
```kotlin
// 只在狀態變化時記錄 log
// Log.d(TAG, "processFaceLandmarks: 開始處理，特徵點數量=${faceLandmarks.size}")

// 校正模式處理
if (isCalibrating) {
    handleCalibration(faceLandmarks, currentTime)
    return FatigueDetectionResult(
        isFatigueDetected = false,
        fatigueLevel = FatigueLevel.NORMAL,
        events = emptyList()
    )
}
```

### 6. **TODO 和 FIXME 註釋**
```kotlin
// TODO: 優化眨眼檢測算法，提高準確率
// FIXME: 修復在某些設備上的相機初始化問題
// NOTE: 這個方法在低端設備上可能會有性能問題
```

---

## 🎨 代碼風格規範

### 1. **縮進和格式**
```kotlin
// 使用 4 個空格縮進
class FatigueDetector {
    companion object {
        private const val TAG = "FatigueDetector"
    }
    
    private fun detectEyeClosure(
        landmarks: List<NormalizedLandmark>,
        currentTime: Long
    ): FatigueEvent? {
        // 實現...
    }
}
```

### 2. **空行使用**
```kotlin
class FatigueDetector {
    companion object {
        private const val TAG = "FatigueDetector"
    }
    
    // 屬性之間用空行分隔
    private var currentEarThreshold = DEFAULT_EAR_THRESHOLD
    
    private var isEyeClosed = false
    
    // 函數之間用空行分隔
    fun processFaceLandmarks(result: FaceLandmarkerResult): FatigueDetectionResult {
        // 實現...
    }
    
    private fun detectEyeClosure(landmarks: List<NormalizedLandmark>, currentTime: Long): FatigueEvent? {
        // 實現...
    }
}
```

### 3. **導入語句**
```kotlin
// 標準庫導入
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

// Android 框架導入
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

// 第三方庫導入
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

// 項目內部導入
import com.patrick.core.FatigueDetectionResult
import com.patrick.core.FatigueLevel
```

### 4. **Lambda 表達式**
```kotlin
// 單行 Lambda
val events = mutableListOf<FatigueEvent>()
events.forEach { event ->
    when (event) {
        is FatigueEvent.EyeClosure -> fatigueEventCount++
        is FatigueEvent.Yawn -> fatigueEventCount++
    }
}

// 多行 Lambda
val result = faceLandmarker?.detect(mpImage)
result?.let { detectionResult ->
    Log.d(TAG, "檢測到 ${detectionResult.faceLandmarks().size} 個臉部")
    onFaceLandmarksDetected?.invoke(detectionResult)
}
```

---

## 🏛️ 架構設計規範

### 1. **Clean Architecture 分層**

#### **Presentation Layer (表現層)**
```kotlin
// UI 組件
@Composable
fun FatigueMainScreen(
    fatigueLevel: FatigueLevel,
    onUserAcknowledged: () -> Unit
) {
    // UI 實現...
}

// ViewModel
class FatigueViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<FatigueUiState>(FatigueUiState.Normal)
    val uiState: StateFlow<FatigueUiState> = _uiState
    
    fun onFatigueLevelChanged(level: FatigueLevel) {
        // 業務邏輯...
    }
}
```

#### **Domain Layer (領域層)**
```kotlin
// UseCase
class CameraUseCase(private val repository: CameraRepository) {
    val cameraState: StateFlow<CameraRepository.CameraState> = repository.getCameraState()
    
    suspend fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        repository.initializeCamera(previewView, lifecycleOwner)
    }
}

// 業務邏輯
class FatigueDetector {
    fun processFaceLandmarks(result: FaceLandmarkerResult): FatigueDetectionResult {
        // 疲勞檢測邏輯...
    }
}
```

#### **Data Layer (數據層)**
```kotlin
// Repository 接口
interface CameraRepository {
    suspend fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner)
    fun getCameraState(): StateFlow<CameraState>
}

// Repository 實現
class CameraRepositoryImpl(private val context: Context) : CameraRepository {
    override suspend fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        // 實現...
    }
}
```

### 2. **依賴注入規範**
```kotlin
// 工廠模式
object CameraModule {
    fun createCameraModule(context: Context): CameraUseCase {
        val repository = CameraRepositoryImpl(context)
        return CameraUseCase(repository)
    }
}

// 使用方式
class MainActivity : ComponentActivity() {
    private lateinit var cameraUseCase: CameraUseCase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraUseCase = CameraModule.createCameraModule(this)
    }
}
```

### 3. **狀態管理規範**
```kotlin
// 使用 StateFlow 進行響應式狀態管理
class FatigueViewModel : ViewModel() {
    private val _fatigueLevel = MutableStateFlow(FatigueLevel.NORMAL)
    val fatigueLevel: StateFlow<FatigueLevel> = _fatigueLevel
    
    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating
    
    fun updateFatigueLevel(level: FatigueLevel) {
        _fatigueLevel.value = level
    }
}
```

---

## 📦 依賴管理規範

### 1. **Version Catalog 使用**
```toml
# gradle/libs.versions.toml
[versions]
kotlin = "1.9.22"
compileSdk = "36"
targetSdk = "36"
minSdk = "24"

[libraries]
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
mediapipe-tasks-vision = { group = "com.google.mediapipe", name = "tasks-vision", version.ref = "mediapipe" }

[plugins]
android-application = { id = "com.android.application", version = "8.5.0" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

### 2. **模組依賴聲明**
```kotlin
// build.gradle
dependencies {
    implementation(libs.core.ktx)
    implementation(libs.mediapipe.tasks.vision)
    
    // 模組間依賴
    implementation(project(":shared-core"))
    implementation(project(":camera-input"))
    implementation(project(":detection-logic"))
}
```

### 3. **依賴分類**
```kotlin
dependencies {
    // 核心依賴
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    
    // UI 依賴
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    
    // 相機依賴
    implementation(libs.camera.core)
    implementation(libs.camera.view)
    
    // AI 依賴
    implementation(libs.mediapipe.tasks.vision)
    
    // 測試依賴
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
}
```

---

## 🧪 測試規範

### 1. **單元測試**
```kotlin
// 測試文件命名: {ClassName}Test.kt
class FatigueDetectorTest {
    
    @Test
    fun `processFaceLandmarks should return normal when no face detected`() {
        // Given
        val detector = FatigueDetector()
        val emptyResult = createEmptyFaceLandmarkerResult()
        
        // When
        val result = detector.processFaceLandmarks(emptyResult)
        
        // Then
        assertEquals(FatigueLevel.NORMAL, result.fatigueLevel)
        assertFalse(result.isFatigueDetected)
    }
    
    @Test
    fun `detectEyeClosure should detect eye closure when EAR below threshold`() {
        // Given
        val detector = FatigueDetector()
        val landmarks = createLandmarksWithClosedEyes()
        
        // When
        val event = detector.detectEyeClosure(landmarks, System.currentTimeMillis())
        
        // Then
        assertNotNull(event)
        assertTrue(event is FatigueEvent.EyeClosure)
    }
}
```

### 2. **集成測試**
```kotlin
@RunWith(AndroidJUnit4::class)
class CameraIntegrationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun cameraInitializationShouldWork() {
        // Given
        val cameraViewModel = CameraViewModel(ApplicationProvider.getApplicationContext())
        
        // When
        cameraViewModel.initializeCamera(mockPreviewView, mockLifecycleOwner)
        
        // Then
        assertTrue(cameraViewModel.isCameraReady())
    }
}
```

### 3. **測試覆蓋率要求**
- 單元測試覆蓋率：至少 80%
- 集成測試：覆蓋主要用戶流程
- UI 測試：覆蓋關鍵 UI 組件

---

## 📚 文檔規範

### 1. **README.md 結構**
```markdown
# DrowsyGuard

## 項目概述
簡要描述項目功能和目標

## 技術棧
列出主要技術和框架

## 快速開始
安裝和運行說明

## 架構設計
架構圖和設計說明

## 開發指南
開發環境設置和開發流程

## 測試
測試運行說明

## 部署
部署和發布說明

## 貢獻指南
如何參與項目開發

## 許可證
開源許可證信息
```

### 2. **API 文檔**
```kotlin
/**
 * 疲勞檢測管理器
 * 
 * 負責整合疲勞檢測、警報和UI更新功能
 * 
 * ## 使用示例
 * ```kotlin
 * val manager = FatigueDetectionManager(context, uiCallback)
 * manager.startDetection()
 * manager.processFaceLandmarks(result)
 * ```
 * 
 * @param context 應用上下文
 * @param uiCallback UI回調接口
 * 
 * @see FatigueUiCallback
 * @see FatigueDetectionResult
 */
class FatigueDetectionManager(
    private val context: Context,
    private val uiCallback: FatigueUiCallback
)
```

### 3. **架構文檔**
- 使用 Mermaid 圖表描述架構
- 包含依賴關係圖
- 說明數據流向

### 4. **變更日誌**
```markdown
# Changelog

## [1.0.0] - 2024-01-01
### Added
- 初始版本發布
- 疲勞檢測功能
- 相機整合

### Changed
- 無

### Fixed
- 無
```

---

## 🔧 工具配置規範

### 1. **Gradle 配置**
```kotlin
// build.gradle
android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}
```

### 2. **代碼檢查工具**
```kotlin
// 啟用 ktlint
plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

// 啟用 detekt
plugins {
    id("io.gitlab.arturbosch.detekt")
}
```

### 3. **Git 配置**
```gitignore
# .gitignore
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
```

---

## 📋 代碼審查清單

### 1. **功能完整性**
- [ ] 功能實現完整
- [ ] 邊界情況處理
- [ ] 錯誤處理完善

### 2. **代碼質量**
- [ ] 命名規範
- [ ] 註釋完整
- [ ] 代碼簡潔
- [ ] 無重複代碼

### 3. **架構設計**
- [ ] 遵循 Clean Architecture
- [ ] 依賴關係正確
- [ ] 模組職責清晰

### 4. **測試覆蓋**
- [ ] 單元測試完整
- [ ] 集成測試覆蓋
- [ ] 測試用例合理

### 5. **性能考慮**
- [ ] 內存使用合理
- [ ] 執行效率良好
- [ ] 資源釋放及時

---

## 🚀 最佳實踐

### 1. **性能優化**
```kotlin
// 使用 lazy 初始化
private val faceLandmarker by lazy {
    FaceLandmarkerManager.createForRealTime(context)
}

// 避免不必要的對象創建
private val handler = Handler(Looper.getMainLooper())
```

### 2. **內存管理**
```kotlin
// 及時釋放資源
fun cleanup() {
    mediaPlayer?.release()
    mediaPlayer = null
    handler.removeCallbacksAndMessages(null)
}
```

### 3. **錯誤處理**
```kotlin
// 統一的錯誤處理
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

// 使用方式
when (val result = processData()) {
    is Result.Success -> handleSuccess(result.data)
    is Result.Error -> handleError(result.exception)
}
```

### 4. **日誌記錄**
```kotlin
// 使用 TAG 常量
companion object {
    private const val TAG = "FatigueDetector"
}

// 分級日誌
Log.d(TAG, "開始處理圖像")
Log.w(TAG, "檢測到異常情況")
Log.e(TAG, "處理失敗", exception)
```

---

## 📞 聯繫方式

如有任何問題或建議，請聯繫：
- 項目維護者：Patrick
- 郵箱：patrick@example.com
- 項目地址：https://github.com/patrick/DrowsyGuard

---

*最後更新：2024年1月* 