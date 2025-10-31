# 眼睛狀態日誌增強

**日期：** 2025-07-19  
**時間：** 003  
**版本：** 1.0.0  
**類型：** 功能增強

## 📋 需求描述

用戶希望開啟更詳細的眼睛狀態日誌，包括：
- 當前眼睛是否睜開還是閉合
- 眼睛狀態的變化過程
- 眨眼的詳細檢測過程

## 🔧 實施方案

### 增強眼睛狀態檢測日誌

**文件：** `detection-logic/src/main/java/com/patrick/detection/FatigueDetector.kt`

#### 1. 在 `detectEyeClosure` 方法中添加詳細日誌

**新增功能：**
- 每次檢測時記錄 EAR 值和閾值
- 記錄眼睛狀態變化（睜開 ↔ 閉合）
- 記錄眼睛持續閉合的狀態
- 記錄眼睛狀態穩定的情況

**新增日誌類型：**

```kotlin
// 靈敏度日誌 - 記錄每次檢測的 EAR 值
FatigueDetectionLogger.logSensitivity(
    "眼睛狀態檢測",
    earValue = combinedEar,
    earThreshold = currentEarThreshold,
)

// 狀態變化日誌 - 記錄眼睛狀態變化
FatigueDetectionLogger.logEvent(
    "眼睛狀態: $previousStatus -> $eyeStatus",
    eventType = "EyeStatus",
    duration = if (isEyeClosed) currentTime - lastEyeClosureStartTime else null,
)

// 持續閉合日誌 - 記錄眼睛持續閉合的狀態
FatigueDetectionLogger.logEvent(
    "眼睛持續閉合中",
    eventType = "EyeClosureOngoing",
    duration = closureDuration,
)

// 狀態穩定日誌 - 記錄眼睛狀態沒有變化的情況
FatigueDetectionLogger.logEvent(
    "眼睛狀態穩定: $eyeStatus",
    eventType = "EyeStatusStable",
)
```

#### 2. 在 `detectBlink` 方法中添加詳細日誌

**新增功能：**
- 記錄眨眼檢測的檢查過程
- 記錄眨眼間隔時間
- 記錄跳過的眨眼（間隔太短）

**新增日誌類型：**

```kotlin
// 眨眼檢查日誌 - 記錄眨眼檢測的檢查過程
FatigueDetectionLogger.logEvent(
    "眨眼檢測檢查",
    eventType = "BlinkCheck",
    duration = timeSinceLastBlink,
)

// 眨眼跳過日誌 - 記錄因間隔太短而跳過的眨眼
FatigueDetectionLogger.logEvent(
    "眨眼間隔太短，跳過計數",
    eventType = "BlinkSkipped",
    duration = timeSinceLastBlink,
)
```

## 📊 日誌輸出示例

### 眼睛狀態檢測日誌

```
[14:30:15.123] [SENSITIVITY] 眼睛狀態檢測 | EAR=0.18 | EAR_Threshold=0.15
[14:30:15.123] [EVENT] 眼睛狀態: 睜開 -> 睜開 | Type=EyeStatusStable
[14:30:15.456] [SENSITIVITY] 眼睛狀態檢測 | EAR=0.12 | EAR_Threshold=0.15
[14:30:15.456] [EVENT] 眼睛狀態: 睜開 -> 閉合 | Type=EyeStatus
[14:30:15.456] [EVENT] 眼睛開始閉合 | Type=EyeClosureStart
[14:30:15.789] [SENSITIVITY] 眼睛狀態檢測 | EAR=0.11 | EAR_Threshold=0.15
[14:30:15.789] [EVENT] 眼睛狀態: 閉合 -> 閉合 | Type=EyeStatus | Duration=333ms
[14:30:15.789] [EVENT] 眼睛持續閉合中 | Type=EyeClosureOngoing | Duration=333ms
[14:30:16.123] [SENSITIVITY] 眼睛狀態檢測 | EAR=0.19 | EAR_Threshold=0.15
[14:30:16.123] [EVENT] 眼睛狀態: 閉合 -> 睜開 | Type=EyeStatus | Duration=667ms
[14:30:16.123] [EVENT] 眨眼檢測 | Type=Blink | Duration=667ms
```

### 眨眼檢測日誌

```
[14:30:16.123] [EVENT] 眨眼檢測檢查 | Type=BlinkCheck | Duration=667ms
[14:30:16.123] [EVENT] 眨眼檢測成功 | Type=Blink | BlinkCount=5
[14:30:16.234] [EVENT] 眨眼檢測檢查 | Type=BlinkCheck | Duration=111ms
[14:30:16.234] [EVENT] 眨眼間隔太短，跳過計數 | Type=BlinkSkipped | Duration=111ms
```

## ✅ 功能特點

### 1. **詳細的狀態追蹤**
- 每次檢測都記錄 EAR 值和閾值
- 清楚顯示眼睛狀態變化過程
- 記錄狀態持續時間

### 2. **完整的眨眼檢測流程**
- 記錄眨眼檢測的每個步驟
- 顯示眨眼間隔時間
- 說明跳過眨眼的原因

### 3. **便於調試**
- 結構化的日誌格式
- 清晰的時間戳
- 詳細的狀態信息

### 4. **性能友好**
- 使用現有的 `FatigueDetectionLogger` 系統
- 可通過日誌開關控制輸出
- 不影響檢測性能

## 🔍 使用方式

### 查看日誌

在 Android Studio 的 Logcat 中，使用以下標籤過濾日誌：

```
FatigueDetection
```

### 日誌類型過濾

- **靈敏度日誌：** `[SENSITIVITY]`
- **事件日誌：** `[EVENT]`
- **眼睛狀態：** `Type=EyeStatus`
- **眨眼檢測：** `Type=Blink`

### 日誌開關控制

可以通過 `FatigueDetectionManager.setLogEnabled()` 方法控制日誌輸出：

```kotlin
fatigueDetectionManager.setLogEnabled(
    sensitivity = true,  // 靈敏度日誌
    event = true,        // 事件日誌
    // 其他日誌類型...
)
```

## 🎯 預期效果

1. **更好的調試體驗**：開發者可以清楚看到眼睛狀態的變化過程
2. **問題診斷**：可以快速定位眼睛檢測的問題
3. **參數調優**：可以根據實際的 EAR 值調整閾值
4. **用戶體驗改善**：通過詳細日誌可以優化檢測準確性

## 📝 注意事項

1. **日誌量增加**：由於增加了詳細日誌，日誌輸出量會增加
2. **性能影響**：日誌記錄對性能影響很小，但建議在生產環境中適當關閉
3. **存儲空間**：長時間運行時需要注意日誌存儲空間

## 🔄 後續優化

1. **日誌級別控制**：可以考慮添加更細粒度的日誌級別控制
2. **日誌持久化**：可以考慮將重要日誌保存到文件
3. **實時監控**：可以考慮添加實時的眼睛狀態監控界面 