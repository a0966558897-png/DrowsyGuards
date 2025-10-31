# 疲勞偵測日誌產生器快速開始指南

**版本**: 1.0.0  
**日期**: 2025年1月27日  

## 🚀 立即開始使用

### 1. 查看日誌

#### 在 Android Studio 中查看日誌

1. **打開 Logcat 窗口**
   - 點擊底部的 **Logcat** 標籤
   - 或者使用快捷鍵 `Alt + 6`

2. **設置過濾器**
   - 在過濾器框中輸入：`FatigueDetection`
   - 或者選擇 **Debug** 級別

3. **運行應用**
   - 啟動疲勞偵測功能
   - 觀察日誌輸出

#### 您會看到的日誌示例

```
[14:30:25.123] [SENSITIVITY] 檢測結果 | EAR=0.145 | EAR_Threshold=0.15 | MAR=0.65 | MAR_Threshold=0.7 | EventCount=2 | EventThreshold=1
[14:30:25.456] [TRIGGER] 疲勞級別變化 | Level=WARNING | PreviousLevel=NORMAL | Reason=EyeClosure
[14:30:25.789] [CALIBRATION] 校正完成 | Progress=100% | MinEAR=0.12 | MaxEAR=0.18 | AvgEAR=0.15 | NewThreshold=0.105 | SampleCount=150
```

### 2. 在您的 Activity 中使用

#### 基本使用

```kotlin
class MainActivity : ComponentActivity() {
    private val viewModel: FatigueScreenViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 設置調試模式
        viewModel.setDebugMode("quick")
        
        // 其他初始化代碼...
    }
    
    // 生成調試報告
    private fun generateReport() {
        val report = viewModel.generateDebugReport()
        Log.d("Debug", report)
        
        // 或者顯示在 UI 中
        textView.text = report
    }
    
    // 保存報告到文件
    private fun saveReport() {
        val filePath = viewModel.saveDebugReport()
        Log.d("Debug", "報告已保存: $filePath")
    }
}
```

#### 添加調試按鈕

```kotlin
@Composable
fun DebugButtons(viewModel: FatigueScreenViewModel) {
    Column {
        Button(onClick = { viewModel.setDebugMode("quick") }) {
            Text("快速調試模式")
        }
        
        Button(onClick = { viewModel.setDebugMode("sensitivity") }) {
            Text("靈敏度調試模式")
        }
        
        Button(onClick = { viewModel.setDebugMode("performance") }) {
            Text("性能調試模式")
        }
        
        Button(onClick = { viewModel.setDebugMode("off") }) {
            Text("關閉日誌")
        }
        
        Button(onClick = { 
            val report = viewModel.generateDebugReport()
            Log.d("Debug", report)
        }) {
            Text("生成報告")
        }
        
        Button(onClick = { 
            val filePath = viewModel.saveDebugReport()
            Log.d("Debug", "保存到: $filePath")
        }) {
            Text("保存報告")
        }
    }
}
```

## 🎯 調試模式說明

### 快速調試模式（推薦）
```kotlin
viewModel.setDebugMode("quick")
```
- ✅ 啟用所有日誌類型
- ✅ 自動生成報告（每30秒）
- ✅ 保存到文件
- 🎯 適合：一般問題排查

### 靈敏度調試模式
```kotlin
viewModel.setDebugMode("sensitivity")
```
- ✅ 專注於靈敏度相關日誌
- ✅ 詳細的 EAR/MAR 值記錄
- ✅ 校正過程追蹤
- 🎯 適合：調整 EAR、MAR 閾值

### 性能調試模式
```kotlin
viewModel.setDebugMode("performance")
```
- ✅ 只記錄關鍵觸發事件
- ✅ 減少日誌噪音
- ✅ 專注於性能指標
- 🎯 適合：性能優化

### 關閉日誌
```kotlin
viewModel.setDebugMode("off")
```
- ✅ 關閉所有調試日誌
- 🎯 適合：生產環境

## 📊 日誌分析技巧

### 1. 過濾特定日誌

在 Logcat 中使用過濾器：

| 過濾器 | 說明 |
|--------|------|
| `[SENSITIVITY]` | 靈敏度相關日誌 |
| `[TRIGGER]` | 觸發邏輯日誌 |
| `[CALIBRATION]` | 校正相關日誌 |
| `[EVENT]` | 事件相關日誌 |
| `[RESET]` | 重置相關日誌 |

### 2. 分析 EAR 值

觀察 EAR 值的變化趨勢：
```
EAR=0.145 → EAR=0.142 → EAR=0.138 → EAR=0.135
```
- 📉 逐漸降低：可能表示眼睛正在閉合
- 📈 逐漸升高：可能表示眼睛正在睜開
- 🔄 波動：可能是眨眼

### 3. 分析事件計數

觀察事件計數的變化：
```
EventCount=0 → EventCount=1 → EventCount=2
```
- ⚡ 快速增加：可能需要調整閾值
- 🐌 緩慢增加：檢測可能過於嚴格
- ❌ 不增加：可能漏檢

## 🔧 常見問題解決

### 問題1：日誌太多，影響性能

**解決方案**：
```kotlin
// 使用性能調試模式
viewModel.setDebugMode("performance")

// 或者關閉特定日誌
viewModel.setDebugMode("off")
```

### 問題2：找不到日誌

**解決方案**：
1. 確保過濾器設置正確：`FatigueDetection`
2. 確保日誌級別設置為 **Debug**
3. 確保調試模式已啟用

### 問題3：報告生成失敗

**解決方案**：
```kotlin
try {
    val report = viewModel.generateDebugReport()
    Log.d("Debug", report)
} catch (e: Exception) {
    Log.e("Debug", "報告生成失敗", e)
}
```

## 📱 實際使用場景

### 場景1：調試誤檢問題

**步驟**：
1. 設置靈敏度調試模式
2. 觀察 EAR 值是否過低
3. 生成報告查看建議
4. 調整 EAR 閾值

```kotlin
viewModel.setDebugMode("sensitivity")
// 運行一段時間後
val report = viewModel.generateDebugReport()
// 根據報告建議調整參數
```

### 場景2：調試漏檢問題

**步驟**：
1. 設置快速調試模式
2. 觀察事件計數
3. 檢查觸發邏輯
4. 調整事件閾值

```kotlin
viewModel.setDebugMode("quick")
// 運行一段時間後
val report = viewModel.generateDebugReport()
// 根據報告建議調整參數
```

### 場景3：調試校正問題

**步驟**：
1. 設置靈敏度調試模式
2. 觀察校正過程
3. 檢查校正結果
4. 重新校正（如需要）

```kotlin
viewModel.setDebugMode("sensitivity")
// 等待校正完成
val report = viewModel.generateDebugReport()
// 檢查校正效果
```

## 📋 調試檢查清單

### 開始調試前
- [ ] 選擇合適的調試模式
- [ ] 設置 Logcat 過濾器
- [ ] 準備記錄問題現象

### 調試過程中
- [ ] 觀察日誌輸出
- [ ] 記錄關鍵數據
- [ ] 生成調試報告
- [ ] 分析調整建議

### 調試完成後
- [ ] 保存重要報告
- [ ] 記錄參數調整
- [ ] 驗證調整效果
- [ ] 關閉調試日誌

## 🎉 總結

這個日誌產生器提供了：

1. **🔍 多種調試模式**：適應不同調試需求
2. **📝 結構化日誌**：便於快速定位問題
3. **🤖 智能報告**：自動生成調整建議
4. **⚙️ 靈活配置**：可控制的日誌開關
5. **💾 文件保存**：便於後續分析

**立即開始使用**：
```kotlin
// 在您的 Activity 中
viewModel.setDebugMode("quick")
```

然後在 Logcat 中查看 `FatigueDetection` 日誌，開始調試您的疲勞偵測系統！ 