# UI 架構分析與改進建議

**日期：** 2025-07-19  
**時間：** 001  
**版本：** 1.0.0  
**類型：** 架構分析

## 📋 當前問題

用戶反映 UI 狀態更新不同步，經過分析發現存在兩個不同的 UI 更新機制，導致狀態管理混亂。

## 🔍 架構分析

### 當前架構問題

#### 1. **重複的 UI 狀態管理**

**問題：** 存在兩個不同的 FatigueViewModel

```kotlin
// app/src/main/java/com/patrick/main/ui/FatigueViewModel.kt
class FatigueViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<FatigueUiState>(FatigueUiState.Calibrating)
    private val _statusText = MutableStateFlow("校正中，請自然眨眼 15 秒…")
    
    fun onNoticeFatigue() {
        _uiState.value = FatigueUiState.NoticeAlert
        _statusText.value = "提醒"
    }
}

// ui-components/src/main/java/com/patrick/ui/fatigue/FatigueViewModel.kt
class FatigueViewModel : ViewModel() {
    private val _fatigueLevel = MutableStateFlow(FatigueLevel.NORMAL)
    private val _statusText = MutableStateFlow("持續偵測中…")
    
    override fun onNoticeFatigue() {
        val processedLevel = fatigueUiStateManager.processFatigueResult(
            FatigueLevel.NOTICE,
            fatigueDetectionManager.getFatigueEventCount()
        )
        updateUIState(processedLevel, false, "注意")
    }
}
```

#### 2. **狀態類型不一致**

- **app 模組**：使用 `FatigueUiState` (sealed class)
- **ui-components 模組**：使用 `FatigueLevel` (enum)

#### 3. **處理邏輯不同**

- **app 模組**：直接更新狀態
- **ui-components 模組**：通過 `FatigueUiStateManager` 處理

### 架構問題的影響

1. **狀態同步問題**：兩個 ViewModel 可能狀態不一致
2. **維護困難**：需要同時維護兩套邏輯
3. **代碼重複**：違反 DRY 原則
4. **混淆性**：開發者容易混淆職責分工

## 🎯 改進建議

### 方案一：統一狀態管理（推薦）

#### 目標
- 統一所有 UI 狀態管理到一個地方
- 消除重複代碼
- 確保狀態一致性

#### 實施步驟

1. **選擇主要 ViewModel**
   - 保留 `ui-components` 中的 `FatigueViewModel`
   - 移除 `app` 模組中的 `FatigueViewModel`

2. **統一狀態類型**
   ```kotlin
   // 統一使用 FatigueLevel
   enum class FatigueLevel {
       NORMAL, NOTICE, WARNING
   }
   ```

3. **統一處理邏輯**
   ```kotlin
   class FatigueViewModel : ViewModel() {
       private val _fatigueLevel = MutableStateFlow(FatigueLevel.NORMAL)
       private val _statusText = MutableStateFlow("持續偵測中…")
       private val _showFatigueDialog = MutableStateFlow(false)
       
       override fun onNoticeFatigue() {
           val processedLevel = fatigueUiStateManager.processFatigueResult(
               FatigueLevel.NOTICE,
               fatigueDetectionManager.getFatigueEventCount()
           )
           updateUIState(processedLevel, false, "注意")
       }
   }
   ```

4. **更新 UI 組件**
   ```kotlin
   // 所有 UI 組件都使用統一的 ViewModel
   @Composable
   fun FatigueMainScreen(
       fatigueViewModel: FatigueViewModel = viewModel()
   ) {
       val fatigueLevel by fatigueViewModel.currentFatigueLevel.collectAsState()
       val statusText by fatigueViewModel.statusText.collectAsState()
       // ...
   }
   ```

### 方案二：職責分離（備選）

#### 目標
- 明確兩個 ViewModel 的職責分工
- 避免狀態重複

#### 職責分工

1. **app 模組 FatigueViewModel**
   - 負責應用級別的狀態（校正、設定等）
   - 管理應用生命週期相關狀態

2. **ui-components 模組 FatigueViewModel**
   - 負責疲勞檢測相關的 UI 狀態
   - 管理疲勞級別、對話框等

#### 實施方式

```kotlin
// app 模組：應用級別狀態
class AppFatigueViewModel : ViewModel() {
    private val _isCalibrating = MutableStateFlow(false)
    private val _calibrationProgress = MutableStateFlow(0)
    
    fun onCalibrationStarted() {
        _isCalibrating.value = true
        _calibrationProgress.value = 0
    }
}

// ui-components 模組：疲勞檢測狀態
class FatigueDetectionViewModel : ViewModel() {
    private val _fatigueLevel = MutableStateFlow(FatigueLevel.NORMAL)
    private val _showFatigueDialog = MutableStateFlow(false)
    
    override fun onNoticeFatigue() {
        // 疲勞檢測相關邏輯
    }
}
```

## 📊 方案比較

| 方案 | 優點 | 缺點 | 推薦度 |
|------|------|------|--------|
| 統一狀態管理 | 消除重複、狀態一致、維護簡單 | 需要重構、可能影響模組化 | ⭐⭐⭐⭐⭐ |
| 職責分離 | 保持模組化、職責清晰 | 仍有重複、複雜度增加 | ⭐⭐⭐ |

## 🚀 實施計劃

### 階段一：分析與準備（1-2 天）
1. 詳細分析當前架構
2. 確定最終方案
3. 制定重構計劃

### 階段二：重構實施（3-5 天）
1. 統一狀態管理
2. 更新 UI 組件
3. 修復相關問題

### 階段三：測試與驗證（1-2 天）
1. 功能測試
2. UI 同步測試
3. 性能測試

## 📝 結論

當前架構存在明顯的問題，建議採用**統一狀態管理**方案。雖然需要一定的重構工作，但能夠：

1. **解決根本問題**：消除狀態同步問題
2. **改善維護性**：減少重複代碼
3. **提高開發效率**：降低開發者混淆
4. **確保一致性**：統一的狀態管理邏輯

重構後，整個 UI 狀態管理將更加清晰、一致和易於維護。 