# 測試指南

## 概述

本文檔描述了 DrowsyGuard 項目的測試策略、測試框架和最佳實踐。

## 測試架構

### 測試金字塔

```
    🎨 UI 測試 (少量)
   🔗 集成測試 (適量)
  🧪 單元測試 (大量)
```

### 測試類型

1. **單元測試** (`src/test/`)
   - 測試單個類或函數
   - 快速執行，高覆蓋率
   - 使用 MockK 進行模擬

2. **集成測試** (`src/androidTest/`)
   - 測試組件間的協調
   - 使用真實的 Android 環境
   - 驗證組件集成

3. **UI 測試** (`src/androidTest/`)
   - 測試用戶界面
   - 使用 Compose Test
   - 驗證用戶交互

## 測試框架

### 依賴

```kotlin
// 單元測試
testImplementation libs.junit
testImplementation libs.mockk
testImplementation libs.turbine
testImplementation libs.coroutines.test

// Android 測試
androidTestImplementation libs.androidx.test.ext
androidTestImplementation libs.espresso
androidTestImplementation libs.androidx.compose.ui.test.junit4

// 測試覆蓋率
kover
```

### 基礎類

#### BaseTest
```kotlin
abstract class BaseTest {
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
```

#### MockKTest
```kotlin
abstract class MockKTest {
    @BeforeEach
    fun setUpMockK() {
        MockKAnnotations.init(this)
    }
    
    @AfterEach
    fun tearDownMockK() {
        unmockkAll()
    }
}
```

### 測試工具

#### TestUtils
```kotlin
object TestUtils {
    fun getTestContext(): Context
    fun createFatigueDetectionResult(...): FatigueDetectionResult
    fun createNormalFatigueResult(): FatigueDetectionResult
    fun createNoticeFatigueResult(): FatigueDetectionResult
    fun createWarningFatigueResult(): FatigueDetectionResult
}
```

## 測試最佳實踐

### 1. 測試命名

使用描述性的測試名稱，格式：`should_expectedBehavior_when_condition`

```kotlin
@Test
fun `should play warning sound when fatigue level is notice`() {
    // 測試實現
}
```

### 2. 測試結構

使用 Given-When-Then 模式：

```kotlin
@Test
fun `should trigger vibration when permission granted`() = runTest {
    // Given - 設置測試條件
    every { mockContext.checkSelfPermission(any()) } returns PackageManager.PERMISSION_GRANTED
    
    // When - 執行被測試的方法
    vibrationManager.triggerVibration()
    
    // Then - 驗證結果
    verify {
        mockVibrator.vibrate(VibrationManager.VIBRATION_DURATION_MS)
    }
}
```

### 3. 模擬策略

- 使用 MockK 進行模擬
- 只模擬外部依賴
- 避免過度模擬

```kotlin
// 好的模擬
every { mockContext.getSystemService(Context.VIBRATOR_SERVICE) } returns mockVibrator

// 避免過度模擬
// every { mockContext.toString() } returns "MockContext" // 不需要
```

### 4. 異常測試

測試異常情況：

```kotlin
@Test
fun `should handle security exception gracefully`() = runTest {
    // Given
    every { mockVibrator.vibrate(any()) } throws SecurityException("Permission denied")
    
    // When & Then
    assertDoesNotThrow {
        vibrationManager.triggerVibration()
    }
}
```

### 5. 異步測試

使用 `runTest` 和 `TestDispatcher`：

```kotlin
@Test
fun `should handle async operation`() = runTest {
    // 測試異步代碼
}
```

## 運行測試

### 命令行

```bash
# 運行所有測試
./scripts/run-tests.sh

# 運行特定類型測試
./scripts/run-tests.sh unit
./scripts/run-tests.sh integration
./scripts/run-tests.sh ui
./scripts/run-tests.sh coverage
```

### Gradle 任務

```bash
# 單元測試
./gradlew test

# 集成測試
./gradlew connectedAndroidTest

# 測試覆蓋率
./gradlew koverReport
```

## 測試覆蓋率

### 目標覆蓋率

- **單元測試**: 80%
- **集成測試**: 70%
- **UI 測試**: 60%

### 查看覆蓋率報告

```bash
./gradlew koverReport
# 報告位置: build/reports/kover/
```

## 測試文件組織

```
src/
├── test/                    # 單元測試
│   └── java/
│       └── com/patrick/
│           ├── alert/
│           │   ├── SoundManagerTest.kt
│           │   ├── VibrationManagerTest.kt
│           │   └── VisualAlertManagerTest.kt
│           └── core/
│               ├── BaseTest.kt
│               └── TestUtils.kt
└── androidTest/             # 集成和 UI 測試
    └── java/
        └── com/patrick/
            ├── alert/
            │   └── FatigueAlertManagerIntegrationTest.kt
            └── main/ui/
                └── FatigueMainScreenUITest.kt
```

## 持續集成

### GitHub Actions

```yaml
- name: Run Tests
  run: |
    ./scripts/run-tests.sh all
```

### 測試門檻

- 所有測試必須通過
- 測試覆蓋率不低於目標
- 沒有測試相關的警告

## 故障排除

### 常見問題

1. **測試失敗**
   - 檢查模擬設置
   - 驗證測試環境
   - 查看詳細錯誤信息

2. **覆蓋率低**
   - 添加邊界條件測試
   - 測試異常情況
   - 檢查未覆蓋的代碼路徑

3. **測試緩慢**
   - 使用適當的模擬
   - 避免不必要的異步操作
   - 優化測試設置

### 調試技巧

```kotlin
// 啟用詳細日誌
@Test
fun `debug test`() {
    println("Debug information")
    // 測試代碼
}
```

## 參考資源

- [Android Testing Guide](https://developer.android.com/training/testing)
- [MockK Documentation](https://mockk.io/)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/) 