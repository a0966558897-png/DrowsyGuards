# 疲勞偵測調試指南

**版本**: 1.0.0  
**日期**: 2025年1月27日  
**適用對象**: 開發者、測試人員  

## 概述

本指南介紹如何使用新的日誌系統來調試疲勞偵測的靈敏度和觸發邏輯，幫助您快速定位問題並優化檢測參數。

## 日誌系統架構

### 1. 日誌管理器 (`FatigueDetectionLogger`)

提供結構化的日誌記錄，分為以下幾個類別：

- **靈敏度日誌 (SENSITIVITY)**: EAR、MAR 值，閾值，事件計數
- **觸發日誌 (TRIGGER)**: 疲勞級別變化，觸發原因
- **校正日誌 (CALIBRATION)**: 校正進度，EAR 統計
- **事件日誌 (EVENT)**: 眨眼、閉眼、打哈欠等事件
- **重置日誌 (RESET)**: 重置操作，狀態變化
- **性能日誌 (PERFORMANCE)**: 處理時間，幀率
- **錯誤日誌 (ERROR)**: 異常情況

### 2. 調試工具 (`FatigueDetectionDebugger`)

提供高級調試功能：

- 日誌開關控制
- 自動報告生成
- 參數調整建議
- 文件保存功能

## 快速開始

### 1. 基本使用

```kotlin
// 在您的 Activity 或 ViewModel 中
val debugger = FatigueDetectionDebugger(context)

// 啟用快速調試模式
debugger.enableQuickDebugMode()

// 生成調試報告
val report = debugger.generateDebugReport(fatigueDetector, fatigueUiStateManager)
Log.d("Debug", report)
```

### 2. 日誌開關控制

```kotlin
// 只啟用靈敏度相關日誌
debugger.setLogEnabled(
    sensitivity = true,
    trigger = false,
    calibration = false,
    event = false,
    reset = false
)

// 或者使用預設模式
debugger.enableSensitivityDebugMode()  // 靈敏度調試
debugger.enablePerformanceDebugMode()  // 性能調試
debugger.disableAllLogs()              // 關閉所有日誌
```

## 調試模式

### 1. 快速調試模式

適用於：一般問題排查

```kotlin
debugger.enableQuickDebugMode()
```

**特點**：
- 啟用所有日誌類型
- 自動生成報告（每30秒）
- 保存到文件
- 適合快速定位問題

### 2. 靈敏度調試模式

適用於：調整 EAR、MAR 閾值

```kotlin
debugger.enableSensitivityDebugMode()
```

**特點**：
- 專注於靈敏度相關日誌
- 詳細的 EAR/MAR 值記錄
- 校正過程追蹤
- 自動生成調整建議

### 3. 性能調試模式

適用於：性能優化

```kotlin
debugger.enablePerformanceDebugMode()
```

**特點**：
- 只記錄關鍵觸發事件
- 減少日誌噪音
- 專注於性能指標

## 日誌分析

### 1. 靈敏度日誌格式

```
[14:30:25.123] [SENSITIVITY] 檢測結果 | EAR=0.145 | EAR_Threshold=0.15 | MAR=0.65 | MAR_Threshold=0.7 | EventCount=2 | EventThreshold=1
```

**分析要點**：
- EAR 值是否接近閾值
- 事件計數是否合理
- 是否需要調整閾值

### 2. 觸發日誌格式

```
[14:30:25.456] [TRIGGER] 疲勞級別變化 | Level=WARNING | PreviousLevel=NORMAL | Reason=EyeClosure
```

**分析要點**：
- 級別變化是否合理
- 觸發原因是否正確
- 頻率是否過高

### 3. 校正日誌格式

```
[14:30:25.789] [CALIBRATION] 校正完成 | Progress=100% | MinEAR=0.12 | MaxEAR=0.18 | AvgEAR=0.15 | NewThreshold=0.105 | SampleCount=150
```

**分析要點**：
- EAR 值範圍是否合理
- 新閾值是否合適
- 樣本數量是否足夠

## 參數調整指南

### 1. EAR 閾值調整

**當前值**: 0.15

**調整建議**：
- **過高 (>0.2)**: 可能漏檢，建議降低到 0.16
- **過低 (<0.1)**: 可能誤檢，建議提高到 0.12
- **合理範圍**: 0.1-0.2

**調試方法**：
```kotlin
// 啟用靈敏度調試模式
debugger.enableSensitivityDebugMode()

// 觀察 EAR 值分佈
// 根據報告中的建議調整
fatigueDetector.setDetectionParameters(earThreshold = 0.12f)
```

### 2. 疲勞事件閾值調整

**當前值**: 1

**調整建議**：
- **過高 (>3)**: 警告觸發較難，建議降低到 2
- **過低 (<1)**: 警告觸發較易，建議提高到 2
- **合理範圍**: 1-3

### 3. 時間閾值調整

**眼睛閉合時間**: 2000ms

**調整建議**：
- **過長 (>3000ms)**: 建議縮短到 1500ms
- **過短 (<1000ms)**: 建議延長到 2500ms
- **合理範圍**: 1000-3000ms

## 常見問題排查

### 1. 誤檢問題

**症狀**: 正常狀態下頻繁觸發警告

**排查步驟**：
1. 啟用靈敏度調試模式
2. 觀察 EAR 值分佈
3. 檢查閾值是否過低
4. 調整 EAR 閾值

```kotlin
// 提高 EAR 閾值
fatigueDetector.setDetectionParameters(earThreshold = 0.18f)
```

### 2. 漏檢問題

**症狀**: 疲勞狀態下不觸發警告

**排查步驟**：
1. 啟用靈敏度調試模式
2. 觀察 EAR 值分佈
3. 檢查閾值是否過高
4. 調整 EAR 閾值

```kotlin
// 降低 EAR 閾值
fatigueDetector.setDetectionParameters(earThreshold = 0.12f)
```

### 3. 校正問題

**症狀**: 校正後檢測不準確

**排查步驟**：
1. 檢查校正日誌
2. 確認樣本數量足夠
3. 檢查 EAR 值範圍
4. 重新校正

```kotlin
// 重新校正
fatigueDetector.reset()
// 等待重新校正
```

### 4. 性能問題

**症狀**: 應用卡頓，日誌過多

**排查步驟**：
1. 啟用性能調試模式
2. 觀察處理時間
3. 關閉不必要的日誌
4. 優化檢測頻率

```kotlin
// 關閉詳細日誌
debugger.enablePerformanceDebugMode()
```

## 報告分析

### 1. 靈敏度報告

```
=== 疲勞偵測靈敏度調試報告 ===
EAR 統計 (1000 個樣本):
  平均值: 0.145
  最小值: 0.08
  最大值: 0.22
  當前閾值: 0.15

=== 調整建議 ===
建議 EAR 閾值: 0.116 (基於平均值 0.145)
```

**分析要點**：
- 平均值是否在合理範圍
- 最小值是否過低
- 最大值是否過高
- 建議閾值是否合理

### 2. 事件統計

```
疲勞事件統計 (50 個事件):
  眼睛閉合: 30
  打哈欠: 15
  高眨眼頻率: 5
```

**分析要點**：
- 事件分佈是否合理
- 某類事件是否過多
- 是否需要調整時間閾值

## 最佳實踐

### 1. 調試流程

1. **問題識別**: 確定具體問題（誤檢、漏檢、性能等）
2. **模式選擇**: 選擇合適的調試模式
3. **數據收集**: 收集足夠的日誌數據
4. **分析報告**: 分析生成的調試報告
5. **參數調整**: 根據建議調整參數
6. **驗證效果**: 驗證調整後的效果

### 2. 日誌管理

- **開發階段**: 啟用詳細日誌
- **測試階段**: 使用快速調試模式
- **生產環境**: 關閉所有調試日誌
- **問題排查**: 按需啟用特定日誌

### 3. 參數調整

- **小步調整**: 每次調整幅度不要太大
- **充分測試**: 調整後要進行充分測試
- **記錄變更**: 記錄每次參數變更
- **回滾準備**: 保留原始參數以便回滾

## 工具集成

### 1. 在 ViewModel 中使用

```kotlin
class FatigueViewModel(application: Application) : AndroidViewModel(application) {
    private val debugger = FatigueDetectionDebugger(application)
    
    init {
        // 根據環境設置調試模式
        if (BuildConfig.DEBUG) {
            debugger.enableQuickDebugMode()
        }
    }
    
    fun generateDebugReport(): String {
        return debugger.generateDebugReport(fatigueDetector, fatigueUiStateManager)
    }
}
```

### 2. 在 Activity 中使用

```kotlin
class MainActivity : ComponentActivity() {
    private val debugger = FatigueDetectionDebugger(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 添加調試菜單
        if (BuildConfig.DEBUG) {
            addDebugMenu()
        }
    }
    
    private fun addDebugMenu() {
        // 添加調試選項到菜單
    }
}
```

## 故障排除

### 1. 日誌不顯示

**可能原因**：
- 日誌級別設置過高
- 日誌開關未啟用
- 過濾器設置不當

**解決方法**：
```kotlin
// 確保日誌開關已啟用
debugger.setLogEnabled(sensitivity = true)

// 檢查日誌級別
Log.d("FatigueDetection", "測試日誌")
```

### 2. 報告生成失敗

**可能原因**：
- 權限不足
- 存儲空間不足
- 文件路徑錯誤

**解決方法**：
```kotlin
// 檢查權限
if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
    debugger.saveDebugReport(report)
}

// 使用內部存儲
val file = File(filesDir, "debug_report.txt")
file.writeText(report)
```

### 3. 性能影響

**可能原因**：
- 日誌過於詳細
- 自動報告頻率過高
- 文件 I/O 過多

**解決方法**：
```kotlin
// 使用性能調試模式
debugger.enablePerformanceDebugMode()

// 降低報告頻率
debugger.setDebugConfig(
    debugger.debugConfig.copy(reportInterval = 300000L) // 5分鐘
)
```

## 總結

新的日誌系統為疲勞偵測調試提供了強大的工具：

1. **結構化日誌**: 分類清晰，便於分析
2. **智能建議**: 自動生成調整建議
3. **靈活配置**: 多種調試模式
4. **報告生成**: 自動生成調試報告
5. **文件保存**: 便於後續分析

通過合理使用這些工具，您可以快速定位問題、優化參數，提高疲勞偵測的準確性和性能。 