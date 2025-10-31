# 疲勞偵測日誌產生器使用示例

**版本**: 1.0.0  
**日期**: 2025年1月27日  

## 快速開始

### 1. 基本設置

在您的 `FatigueViewModel` 中，日誌產生器已經自動集成：

```kotlin
class FatigueViewModel(application: Application) : AndroidViewModel(application) {
    // 日誌調試工具已自動初始化
    private val debugger = FatigueDetectionDebugger(application)
    
    init {
        // 在 DEBUG 模式下自動啟用快速調試模式
        if (BuildConfig.DEBUG) {
            debugger.enableQuickDebugMode()
        }
    }
}
```

### 2. 查看日誌

#### 在 Android Studio 中查看日誌

1. 打開 **Logcat** 窗口
2. 設置過濾器為 `FatigueDetection`
3. 運行應用並進行疲勞檢測

您會看到類似這樣的日誌：

```
[14:30:25.123] [SENSITIVITY] 檢測結果 | EAR=0.145 | EAR_Threshold=0.15 | MAR=0.65 | MAR_Threshold=0.7 | EventCount=2 | EventThreshold=1
[14:30:25.456] [TRIGGER] 疲勞級別變化 | Level=WARNING | PreviousLevel=NORMAL | Reason=EyeClosure
[14:30:25.789] [CALIBRATION] 校正完成 | Progress=100% | MinEAR=0.12 | MaxEAR=0.18 | AvgEAR=0.15 | NewThreshold=0.105 | SampleCount=150
```

## 調試模式

### 1. 快速調試模式（推薦）

適用於：一般問題排查

```kotlin
// 在您的 Activity 或 ViewModel 中
fatigueViewModel.setDebugMode("quick")
```

**特點**：
- 啟用所有日誌類型
- 自動生成報告（每30秒）
- 保存到文件
- 適合快速定位問題

### 2. 靈敏度調試模式

適用於：調整 EAR、MAR 閾值

```kotlin
fatigueViewModel.setDebugMode("sensitivity")
```

**特點**：
- 專注於靈敏度相關日誌
- 詳細的 EAR/MAR 值記錄
- 校正過程追蹤
- 自動生成調整建議

### 3. 性能調試模式

適用於：性能優化

```kotlin
fatigueViewModel.setDebugMode("performance")
```

**特點**：
- 只記錄關鍵觸發事件
- 減少日誌噪音
- 專注於性能指標

### 4. 關閉日誌

```kotlin
fatigueViewModel.setDebugMode("off")
```

## 生成調試報告

### 1. 手動生成報告

```kotlin
// 在您的 Activity 中
val report = fatigueViewModel.generateDebugReport()
Log.d("Debug", report)

// 或者顯示在 UI 中
textView.text = report
```

### 2. 保存報告到文件

```kotlin
val filePath = fatigueViewModel.saveDebugReport()
Log.d("Debug", "報告已保存到: $filePath")
```

### 3. 自動報告

當啟用自動報告時，系統會定期生成報告：

```kotlin
// 檢查是否有新的自動報告
val autoReport = fatigueViewModel.checkAutoReport()
autoReport?.let { report ->
    Log.d("Debug", "自動報告: $report")
}
```

## 實際使用場景

### 場景1：調試誤檢問題

**問題**：正常狀態下頻繁觸發警告

**解決步驟**：

1. **啟用靈敏度調試模式**
```kotlin
fatigueViewModel.setDebugMode("sensitivity")
```

2. **觀察 EAR 值**
在 Logcat 中查看：
```
[14:30:25.123] [SENSITIVITY] 檢測結果 | EAR=0.145 | EAR_Threshold=0.15
```

3. **生成報告分析**
```kotlin
val report = fatigueViewModel.generateDebugReport()
Log.d("Debug", report)
```

4. **根據建議調整參數**
```kotlin
// 如果報告建議提高 EAR 閾值
fatigueDetectionManager.setDetectionParameters(
    earThreshold = 0.18f  // 從 0.15 提高到 0.18
)
```

### 場景2：調試漏檢問題

**問題**：疲勞狀態下不觸發警告

**解決步驟**：

1. **啟用快速調試模式**
```kotlin
fatigueViewModel.setDebugMode("quick")
```

2. **觀察觸發邏輯**
在 Logcat 中查看：
```
[14:30:25.456] [TRIGGER] 疲勞級別變化 | Level=NORMAL | PreviousLevel=NORMAL | Reason=正常狀態
```

3. **檢查事件計數**
```
[14:30:25.123] [SENSITIVITY] 檢測結果 | EventCount=0 | EventThreshold=1
```

4. **調整事件閾值**
```kotlin
fatigueDetectionManager.setDetectionParameters(
    fatigueEventThreshold = 1  // 降低閾值
)
```

### 場景3：調試校正問題

**問題**：校正後檢測不準確

**解決步驟**：

1. **啟用靈敏度調試模式**
```kotlin
fatigueViewModel.setDebugMode("sensitivity")
```

2. **觀察校正過程**
```
[14:30:25.789] [CALIBRATION] 校正進行中 | Progress=50% | CurrentEAR=0.13 | SampleCount=75
[14:30:25.890] [CALIBRATION] 校正完成 | Progress=100% | MinEAR=0.12 | MaxEAR=0.18 | AvgEAR=0.15 | NewThreshold=0.105
```

3. **檢查校正結果**
```kotlin
val report = fatigueViewModel.generateDebugReport()
// 查看報告中的 EAR 統計和建議
```

## 日誌分析技巧

### 1. 過濾特定類型的日誌

在 Android Studio Logcat 中使用過濾器：

- `[SENSITIVITY]` - 只看靈敏度相關日誌
- `[TRIGGER]` - 只看觸發邏輯日誌
- `[CALIBRATION]` - 只看校正相關日誌
- `[EVENT]` - 只看事件相關日誌
- `[RESET]` - 只看重置相關日誌

### 2. 分析 EAR 值趨勢

觀察 EAR 值的變化：
```
EAR=0.145 → EAR=0.142 → EAR=0.138 → EAR=0.135
```
如果 EAR 值逐漸降低，可能表示眼睛正在閉合。

### 3. 分析事件觸發頻率

觀察事件計數的變化：
```
EventCount=0 → EventCount=1 → EventCount=2
```
如果事件計數快速增加，可能需要調整閾值。

### 4. 檢查校正效果

比較校正前後的閾值：
```
校正前: EAR_Threshold=0.15
校正後: NewThreshold=0.105
```
如果新閾值過低，可能需要重新校正。

## 常見問題解決

### 問題1：日誌太多，影響性能

**解決方案**：
```kotlin
// 使用性能調試模式
fatigueViewModel.setDebugMode("performance")

// 或者關閉特定類型的日誌
fatigueDetectionManager.setLogEnabled(
    sensitivity = false,  // 關閉靈敏度日誌
    trigger = true,       // 保留觸發日誌
    calibration = false,  // 關閉校正日誌
    event = false,        // 關閉事件日誌
    reset = true          // 保留重置日誌
)
```

### 問題2：報告生成失敗

**解決方案**：
```kotlin
try {
    val report = fatigueViewModel.generateDebugReport()
    Log.d("Debug", report)
} catch (e: Exception) {
    Log.e("Debug", "報告生成失敗", e)
    // 顯示錯誤信息
    textView.text = "報告生成失敗: ${e.message}"
}
```

### 問題3：文件保存失敗

**解決方案**：
```kotlin
// 檢查權限
if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
    val filePath = fatigueViewModel.saveDebugReport()
    Log.d("Debug", "報告已保存: $filePath")
} else {
    // 請求權限
    requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
}
```

## 最佳實踐

### 1. 開發階段
- 使用快速調試模式
- 定期生成報告
- 保存重要報告

### 2. 測試階段
- 使用靈敏度調試模式
- 詳細分析參數調整
- 記錄調整過程

### 3. 生產環境
- 關閉所有調試日誌
- 只保留錯誤日誌
- 監控性能指標

### 4. 問題排查
- 根據問題類型選擇調試模式
- 收集足夠的日誌數據
- 分析報告中的建議

## 總結

這個日誌產生器提供了強大的調試功能：

1. **多種調試模式**：適應不同的調試需求
2. **結構化日誌**：便於快速定位問題
3. **智能報告**：自動生成調整建議
4. **靈活配置**：可控制的日誌開關
5. **文件保存**：便於後續分析

通過合理使用這些工具，您可以大大提高疲勞偵測系統的調試效率和檢測準確性。 