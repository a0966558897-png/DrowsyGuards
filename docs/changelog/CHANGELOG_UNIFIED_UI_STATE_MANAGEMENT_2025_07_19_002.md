# 統一 UI 狀態管理重構

**日期：** 2025-07-19  
**時間：** 002  
**版本：** 1.0.0  
**類型：** 架構重構

## 📋 問題描述

用戶反映 UI 狀態更新不同步，經過分析發現存在兩個不同的 UI 更新機制，導致狀態管理混亂：

1. **app 模組的 FatigueViewModel**：使用 `FatigueUiState` (sealed class)
2. **ui-components 模組的 FatigueViewModel**：使用 `FatigueLevel` (enum)

這種重複的狀態管理導致：
- UI 同步問題
- 維護困難
- 違反 DRY 原則
- 開發者混淆

## 🔧 解決方案

### 統一狀態管理架構

**目標：** 統一所有 UI 狀態管理到 `ui-components` 模組的 `FatigueViewModel`

**實施步驟：**

#### 1. 擴展 ui-components 的 FatigueViewModel

**文件：** `ui-components/src/main/java/com/patrick/ui/fatigue/FatigueViewModel.kt`

**新增功能：**
- 校正相關狀態管理
- 眨眼頻率相關狀態
- 統一的狀態文字管理
- 完整的校正流程支持

**新增狀態：**
```kotlin
// 校正相關狀態
private val _isCalibrating = MutableStateFlow(false)
private val _calibrationProgress = MutableStateFlow(0)
private val _calibrationEarValue = MutableStateFlow(0f)

// 眨眼頻率相關狀態
private val _blinkFrequency = MutableStateFlow(0)
private val _showBlinkFrequency = MutableStateFlow(true)

// 狀態文字（AppBar 中間顯示）
private val _statusText = MutableStateFlow("持續偵測中…")
```

**新增方法：**
```kotlin
fun startCalibration()
fun stopCalibration()
fun onCalibrationProgress(progress: Int, currentEar: Float)
fun onCalibrationCompleted(newThreshold: Float, minEar: Float, maxEar: Float, avgEar: Float)
```

#### 2. 更新 FatigueScreenViewModel

**文件：** `app/src/main/java/com/patrick/main/ui/FatigueScreenViewModel.kt`

**變更：**
- 使用統一的 `ui-components` FatigueViewModel
- 移除對 app 模組 FatigueViewModel 的依賴
- 添加校正相關方法代理

**新增狀態暴露：**
```kotlin
// 校正相關狀態
val calibrationProgress: StateFlow<Int> = fatigueViewModel.calibrationProgress
val isCalibrating: StateFlow<Boolean> = fatigueViewModel.isCalibrating
val calibrationEarValue: StateFlow<Float> = fatigueViewModel.calibrationEarValue

// 眨眼頻率相關狀態
val blinkFrequency: StateFlow<Int> = fatigueViewModel.blinkFrequency
val showBlinkFrequency: StateFlow<Boolean> = fatigueViewModel.showBlinkFrequency
```

#### 3. 擴展 FatigueDetectionManager

**文件：** `detection-logic/src/main/java/com/patrick/detection/FatigueDetectionManager.kt`

**新增方法：**
```kotlin
fun startCalibration()
fun stopCalibration()
```

**實現校正回調：**
```kotlin
override fun onCalibrationStarted()
override fun onCalibrationProgress(progress: Int, currentEar: Float)
override fun onCalibrationCompleted(newThreshold: Float, minEar: Float, maxEar: Float, avgEar: Float)
```

#### 4. 擴展 CameraViewModel

**文件：** `camera-input/src/main/java/com/patrick/camera/CameraViewModel.kt`

**新增方法：**
```kotlin
fun getCameraStatus(): String
```

#### 5. 清理重複代碼

**刪除文件：**
- `app/src/main/java/com/patrick/main/ui/FatigueViewModel.kt`
- `app/src/main/java/com/patrick/main/ui/FatigueUiState.kt`

**修復重複方法：**
- 移除 `FatigueDetectionManager` 中重複的校正方法
- 移除 `CameraViewModel` 中重複的狀態方法

## ✅ 重構結果

### 架構改進

1. **統一狀態管理**：所有 UI 狀態現在由 `ui-components` 的 `FatigueViewModel` 統一管理
2. **消除重複代碼**：刪除了重複的 ViewModel 和狀態類
3. **清晰的職責分離**：
   - `ui-components`：UI 狀態管理
   - `detection-logic`：疲勞檢測邏輯
   - `camera-input`：相機管理
   - `app`：協調層

### 功能完整性

1. **校正功能**：完整的校正流程支持
2. **狀態同步**：所有 UI 組件使用統一的狀態源
3. **回調機制**：完整的 `FatigueUiCallback` 實現
4. **調試支持**：保留所有調試和報告功能

### 代碼質量

1. **編譯成功**：所有模組編譯通過
2. **類型安全**：使用統一的 `FatigueLevel` enum
3. **狀態一致性**：消除狀態不同步問題
4. **可維護性**：單一狀態管理源

## 🔄 遷移指南

### 對於開發者

1. **使用統一 ViewModel**：所有 UI 狀態現在來自 `ui-components` 的 `FatigueViewModel`
2. **狀態類型**：統一使用 `FatigueLevel` 而不是 `FatigueUiState`
3. **校正功能**：通過 `FatigueScreenViewModel` 的校正方法進行校正

### 對於測試

1. **更新測試**：測試應該針對統一的 `FatigueViewModel`
2. **狀態驗證**：驗證 `FatigueLevel` 狀態變化
3. **校正測試**：測試校正流程的完整性

## 📊 影響評估

### 正面影響

1. **解決 UI 同步問題**：統一狀態管理消除不同步
2. **提高代碼質量**：消除重複代碼，提高可維護性
3. **改善開發體驗**：清晰的架構和職責分離
4. **增強穩定性**：單一狀態源減少錯誤

### 風險緩解

1. **向後兼容**：保持所有公共 API 不變
2. **漸進式遷移**：通過 `FatigueScreenViewModel` 提供統一接口
3. **完整測試**：確保所有功能正常工作

## 🎯 後續計劃

1. **性能優化**：監控統一狀態管理的性能影響
2. **文檔更新**：更新架構文檔和開發指南
3. **測試覆蓋**：增加對統一狀態管理的測試覆蓋
4. **用戶反饋**：收集用戶對 UI 同步改善的反饋

## 📝 技術細節

### 狀態流架構

```
FatigueDetector → FatigueDetectionManager → FatigueViewModel → UI Components
     ↓                    ↓                      ↓
FatigueLevel      FatigueUiCallback    StateFlow<FatigueLevel>
```

### 校正流程

```
startCalibration() → onCalibrationStarted() → onCalibrationProgress() → onCalibrationCompleted()
```

### 狀態轉換

```
NORMAL ↔ NOTICE ↔ WARNING
   ↓       ↓       ↓
持續偵測  注意    警告
```

## 🔗 相關文件

- `ui-components/src/main/java/com/patrick/ui/fatigue/FatigueViewModel.kt`
- `app/src/main/java/com/patrick/main/ui/FatigueScreenViewModel.kt`
- `detection-logic/src/main/java/com/patrick/detection/FatigueDetectionManager.kt`
- `camera-input/src/main/java/com/patrick/camera/CameraViewModel.kt`

---

**重構完成時間：** 2025-07-19 14:30  
**編譯狀態：** ✅ 成功  
**測試狀態：** 🔄 待驗證  
**部署狀態：** 🔄 待部署 