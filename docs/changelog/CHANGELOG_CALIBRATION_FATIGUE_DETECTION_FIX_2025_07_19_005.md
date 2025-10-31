# 校正期間疲勞檢測修復

**日期：** 2025-07-19  
**時間：** 005  
**版本：** 1.0.0  
**類型：** 錯誤修復

## 📋 問題描述

用戶反映程式開始時還沒校正化就會有眨眼的偵測，這會導致在還沒校正前就達到警告門檻。具體問題包括：

1. **校正期間眨眼檢測**：即使在校正期間，眨眼檢測仍然會被觸發
2. **校正期間打哈欠檢測**：校正期間仍會進行打哈欠檢測
3. **校正期間眨眼頻率檢測**：校正期間仍會進行眨眼頻率檢測
4. **校正期間眨眼計數**：校正期間仍會進行眨眼計數

這會導致：
- 校正期間錯誤的疲勞事件計數
- 校正完成前就觸發警告
- 影響校正的準確性

## 🔧 解決方案

### 修改所有疲勞檢測方法

**文件：** `detection-logic/src/main/java/com/patrick/detection/FatigueDetector.kt`

#### 1. 修改 `detectEyeClosure` 方法

**新增校正檢查：**
```kotlin
// 校正期間不進行疲勞檢測，包括眨眼檢測
if (isCalibrating || !calibrationStateManager.hasCalibrated()) {
    FatigueDetectionLogger.logEvent(
        "校正期間，跳過眼睛狀態檢測",
        eventType = "CalibrationSkip",
    )
    return null
}
```

**效果：**
- 校正期間不進行眼睛閉合檢測
- 校正期間不進行眨眼檢測
- 避免校正期間的錯誤疲勞事件

#### 2. 修改 `detectYawn` 方法

**新增校正檢查：**
```kotlin
// 校正期間不進行疲勞檢測
if (isCalibrating || !calibrationStateManager.hasCalibrated()) {
    FatigueDetectionLogger.logEvent(
        "校正期間，跳過打哈欠檢測",
        eventType = "CalibrationSkip",
    )
    return null
}
```

**效果：**
- 校正期間不進行打哈欠檢測
- 避免校正期間的錯誤打哈欠計數

#### 3. 修改 `detectBlinkFrequency` 方法

**新增校正檢查：**
```kotlin
// 校正期間不進行疲勞檢測
if (isCalibrating || !calibrationStateManager.hasCalibrated()) {
    FatigueDetectionLogger.logEvent(
        "校正期間，跳過眨眼頻率檢測",
        eventType = "CalibrationSkip",
    )
    return null
}
```

**效果：**
- 校正期間不進行眨眼頻率檢測
- 避免校正期間的錯誤眨眼頻率警告

#### 4. 修改 `detectBlink` 方法

**新增校正檢查：**
```kotlin
// 校正期間不進行眨眼計數
if (isCalibrating || !calibrationStateManager.hasCalibrated()) {
    FatigueDetectionLogger.logEvent(
        "校正期間，跳過眨眼計數",
        eventType = "CalibrationSkip",
    )
    return
}
```

**效果：**
- 校正期間不進行眨眼計數
- 避免校正期間的錯誤眨眼統計

## 📊 校正期間檢測邏輯

### 校正狀態檢查

所有疲勞檢測方法都會檢查以下條件：
```kotlin
if (isCalibrating || !calibrationStateManager.hasCalibrated()) {
    // 跳過檢測
    return null
}
```

**檢查條件：**
- `isCalibrating`：正在進行校正
- `!calibrationStateManager.hasCalibrated()`：尚未完成校正

### 檢測跳過邏輯

當滿足校正條件時：
1. **記錄跳過日誌**：記錄為什麼跳過檢測
2. **返回 null**：不產生任何疲勞事件
3. **不更新計數**：不影響任何計數器

## ✅ 功能特點

### 1. **完整的校正保護**
- 所有疲勞檢測方法都有校正檢查
- 確保校正期間不會產生錯誤的疲勞事件
- 保護校正過程的準確性

### 2. **詳細的日誌記錄**
- 記錄每次跳過檢測的原因
- 便於調試和問題診斷
- 清楚顯示校正期間的檢測狀態

### 3. **智能檢測控制**
- 基於校正狀態的智能檢測控制
- 避免校正期間的干擾
- 確保檢測的準確性

### 4. **向後兼容**
- 不影響已完成的校正狀態
- 保持現有功能的正常運作
- 改善用戶體驗

## 🔍 使用方式

### 校正期間的檢測流程

1. **程式啟動**：檢測到臉部後開始校正
2. **校正進行中**：所有疲勞檢測被跳過
3. **校正完成**：開始正常的疲勞檢測
4. **檢測運行**：正常的疲勞檢測流程

### 日誌輸出示例

```
[14:30:15.123] [EVENT] 校正期間，跳過眼睛狀態檢測 | Type=CalibrationSkip
[14:30:15.456] [EVENT] 校正期間，跳過眨眼計數 | Type=CalibrationSkip
[14:30:15.789] [EVENT] 校正期間，跳過打哈欠檢測 | Type=CalibrationSkip
[14:30:16.012] [EVENT] 校正期間，跳過眨眼頻率檢測 | Type=CalibrationSkip
```

## 🎯 預期效果

1. **避免錯誤警告**：校正期間不會觸發錯誤的疲勞警告
2. **保護校正過程**：校正期間不會受到疲勞檢測的干擾
3. **提高準確性**：確保校正完成後才開始疲勞檢測
4. **改善用戶體驗**：避免校正期間的誤判和干擾

## 📝 注意事項

1. **校正期間保護**：校正期間所有疲勞檢測都會被跳過
2. **日誌記錄**：每次跳過檢測都會記錄詳細日誌
3. **性能影響**：跳過檢測不會影響性能，只是提前返回
4. **狀態一致性**：確保校正狀態和檢測狀態的一致性

## 🔄 後續優化

1. **校正質量評估**：可以考慮添加校正質量的評估機制
2. **檢測閾值調整**：可以考慮根據校正結果動態調整檢測閾值
3. **用戶反饋機制**：可以考慮添加用戶對校正結果的反饋機制
4. **自動重新校正**：可以考慮添加自動重新校正的機制

## 📋 測試建議

1. **校正期間測試**：驗證校正期間不會產生疲勞事件
2. **校正完成後測試**：驗證校正完成後正常進行疲勞檢測
3. **日誌檢查**：檢查校正期間的跳過日誌是否正確記錄
4. **性能測試**：確保修改不會影響檢測性能 