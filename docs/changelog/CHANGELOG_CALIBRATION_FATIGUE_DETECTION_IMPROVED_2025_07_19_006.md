# 校正期間疲勞檢測改進方案

**日期：** 2025-07-19  
**時間：** 006  
**版本：** 1.0.0  
**類型：** 功能改進

## 📋 問題描述

用戶反映程式開始時還沒校正化就會有眨眼的偵測，這會導致在還沒校正前就達到警告門檻。之前的解決方案完全跳過校正期間的檢測，但這樣會影響校正過程，因為校正需要收集 EAR 數據。

**核心問題：**
- 校正期間需要進行眨眼檢測來收集 EAR 數據
- 但校正期間的檢測不應該影響疲勞警告狀態
- 需要在保持校正功能的同時避免錯誤的警告

## 🔧 改進方案

### 設計原則

1. **校正期間正常檢測**：保持眨眼檢測功能，用於收集 EAR 數據
2. **校正期間不更新計數**：避免校正期間的檢測影響疲勞事件計數
3. **校正完成後重置狀態**：確保校正後從乾淨狀態開始檢測

### 具體實現

**文件：** `detection-logic/src/main/java/com/patrick/detection/FatigueDetector.kt`

#### 1. 恢復正常的檢測方法

**移除校正期間的檢測跳過邏輯：**
- `detectEyeClosure`：恢復正常的眼睛狀態檢測
- `detectYawn`：恢復正常的打哈欠檢測
- `detectBlinkFrequency`：恢復正常的眨眼頻率檢測
- `detectBlink`：恢復正常的眨眼計數

**效果：**
- 校正期間正常收集 EAR 數據
- 校正期間正常進行眨眼檢測
- 保持校正過程的完整性

#### 2. 修改 `updateFatigueEventCount` 方法

**新增校正期間檢查：**
```kotlin
// 校正期間不更新疲勞事件計數，避免影響校正過程
if (isCalibrating || !calibrationStateManager.hasCalibrated()) {
    FatigueDetectionLogger.logEvent(
        "校正期間，跳過疲勞事件計數更新",
        eventType = "CalibrationSkip",
    )
    return
}
```

**效果：**
- 校正期間不更新疲勞事件計數
- 校正期間不觸發疲勞警告
- 保持校正過程的穩定性

#### 3. 新增 `resetFatigueEventsAfterCalibration` 方法

**校正完成後重置所有狀態：**
```kotlin
private fun resetFatigueEventsAfterCalibration() {
    val previousEventCount = fatigueEventCount
    val previousBlinkCount = blinkCount
    val previousYawnCount = yawnCount
    val previousBlinkFrequencyWarningCount = blinkFrequencyWarningCount

    // 重置所有疲勞相關狀態
    fatigueEventCount = 0
    blinkCount = 0
    yawnCount = 0
    blinkFrequencyWarningCount = 0
    isEyeClosed = false
    isMouthOpen = false
    lastEyeClosureStartTime = 0
    lastMouthOpenStartTime = 0
    lastBlinkTime = 0
    lastMinuteStartTime = System.currentTimeMillis()
    blinkTimestamps.clear()

    FatigueDetectionLogger.logReset(
        "校正完成後重置疲勞事件計數",
        resetType = "PostCalibrationReset",
        previousEventCount = previousEventCount,
        currentEventCount = fatigueEventCount,
    )
}
```

**效果：**
- 校正完成後重置所有疲勞事件計數
- 確保從乾淨狀態開始疲勞檢測
- 避免校正期間的數據影響後續檢測

#### 4. 修改 `finishCalibration` 方法

**校正完成後自動重置：**
```kotlin
// 校正完成後重置疲勞事件計數，確保從乾淨狀態開始
resetFatigueEventsAfterCalibration()
```

**效果：**
- 校正完成後自動重置疲勞狀態
- 確保校正後立即開始正常的疲勞檢測
- 避免校正期間的干擾

## 📊 校正期間檢測邏輯

### 校正期間的處理流程

1. **正常檢測**：所有疲勞檢測方法正常運行
2. **數據收集**：正常收集 EAR 數據用於校正
3. **計數跳過**：`updateFatigueEventCount` 跳過更新
4. **狀態保護**：疲勞事件計數保持不變

### 校正完成後的處理流程

1. **計算新閾值**：基於收集的 EAR 數據
2. **標記校正完成**：持久化保存校正狀態
3. **重置疲勞狀態**：清空所有疲勞事件計數
4. **開始正常檢測**：使用新閾值進行疲勞檢測

## ✅ 功能特點

### 1. **保持校正完整性**
- 校正期間正常收集 EAR 數據
- 校正期間正常進行眨眼檢測
- 確保校正過程的準確性

### 2. **避免錯誤警告**
- 校正期間不更新疲勞事件計數
- 校正期間不觸發疲勞警告
- 保護校正過程的穩定性

### 3. **確保乾淨開始**
- 校正完成後重置所有疲勞狀態
- 確保校正後從乾淨狀態開始
- 避免校正期間的干擾

### 4. **詳細的日誌記錄**
- 記錄校正期間的跳過操作
- 記錄校正完成後的重置操作
- 便於調試和問題診斷

## 🔍 使用方式

### 校正期間的檢測流程

1. **程式啟動**：檢測到臉部後開始校正
2. **校正進行中**：
   - 正常進行眨眼檢測（收集 EAR 數據）
   - 跳過疲勞事件計數更新
   - 不觸發疲勞警告
3. **校正完成**：
   - 計算新的 EAR 閾值
   - 重置所有疲勞事件計數
   - 開始正常的疲勞檢測

### 日誌輸出示例

```
[14:30:15.123] [EVENT] 校正期間，跳過疲勞事件計數更新 | Type=CalibrationSkip
[14:30:15.456] [CALIBRATION] 校正完成 | Progress=100% | NewThreshold=0.245
[14:30:15.789] [RESET] 校正完成後重置疲勞事件計數 | Type=PostCalibrationReset | PreviousCount=3 | CurrentCount=0
[14:30:16.012] [EVENT] 開始正常疲勞檢測 | Type=DetectionStart
```

## 🎯 預期效果

1. **保持校正功能**：校正期間正常收集 EAR 數據
2. **避免錯誤警告**：校正期間不會觸發錯誤的疲勞警告
3. **確保準確性**：校正完成後使用正確的閾值進行檢測
4. **改善用戶體驗**：避免校正期間的誤判和干擾

## 📝 注意事項

1. **校正期間保護**：校正期間不更新疲勞事件計數，但正常進行檢測
2. **校正完成重置**：校正完成後自動重置所有疲勞狀態
3. **日誌記錄**：詳細記錄校正期間的操作和重置過程
4. **狀態一致性**：確保校正狀態和檢測狀態的一致性

## 🔄 與之前方案的比較

### 之前方案的問題
- 完全跳過校正期間的檢測
- 影響校正過程的完整性
- 無法收集足夠的 EAR 數據

### 改進方案的優勢
- 保持校正期間的正常檢測
- 只跳過疲勞事件計數更新
- 確保校正過程的完整性
- 校正完成後重置狀態

## 📋 測試建議

1. **校正期間測試**：驗證校正期間正常收集 EAR 數據但不觸發警告
2. **校正完成測試**：驗證校正完成後重置狀態並開始正常檢測
3. **日誌檢查**：檢查校正期間的跳過日誌和重置日誌
4. **閾值驗證**：驗證校正後使用正確的新閾值

## 🔄 後續優化

1. **校正質量評估**：可以考慮添加校正質量的評估機制
2. **動態閾值調整**：可以考慮根據校正結果動態調整其他閾值
3. **用戶反饋機制**：可以考慮添加用戶對校正結果的反饋機制
4. **自動重新校正**：可以考慮添加自動重新校正的機制 