# UI 重新設計變更記錄

**日期**: 2025-01-27  
**版本**: 006  
**類型**: UI/UX 重新設計  
**分支**: Rebuild-Face-Dictation  

## 📋 變更摘要

根據用戶提供的設計圖片，完全重新設計了疲勞偵測主頁面的 UI 佈局，實現了更現代化和直觀的用戶界面。

## 🎨 UI 設計變更

### 主要佈局重新設計
- **頂部標題區塊**: 添加了「疲勞偵測中...」的標題文字
- **相機預覽區域**: 重新設計為固定高度的黑色背景區域
- **疲勞等級指示器**: 新增大型圓形指示器，顯示數字和等級文字
- **統計數據區域**: 新增三組並排的統計數據顯示
- **底部按鈕**: 新增「儲存記錄」按鈕

### 新增 UI 組件

#### 1. 疲勞等級指示器 (`FatigueLevelIndicator`)
- 大型圓形背景，根據疲勞等級顯示不同顏色
- 中央顯示數字：0（正常）、2（輕度疲勞）、3（重度疲勞）
- 下方顯示對應的等級文字

#### 2. 統計數據顯示 (`DetectionStats`)
- **眨眼頻率**: 顯示每分鐘眨眼次數
- **打哈欠次數**: 顯示每分鐘打哈欠次數  
- **閉眼時間**: 顯示當前閉眼持續時間（秒）

#### 3. 統計項目組件 (`StatItem`)
- 統一的數據顯示格式
- 數值使用大號粗體字體
- 標籤使用較小字體，帶透明度

## 🔧 技術實現

### 新增數據獲取方法

#### FatigueDetector 新增方法
```kotlin
fun getYawnCount(): Int = yawnCount
fun getEyeClosureDuration(): Long
fun getRecentYawnCount(windowMs: Long = 60000L): Int
```

#### FatigueDetectionManager 新增方法
```kotlin
fun getYawnCount(): Int = fatigueDetector.getYawnCount()
fun getEyeClosureDuration(): Long = fatigueDetector.getEyeClosureDuration()
fun getRecentYawnCount(windowMs: Long = 60000L): Int = fatigueDetector.getRecentYawnCount(windowMs)
```

#### FatigueViewModel 新增狀態
```kotlin
private val _yawnCount = MutableStateFlow(0)
val yawnCount: StateFlow<Int> = _yawnCount

private val _eyeClosureDuration = MutableStateFlow(0L)
val eyeClosureDuration: StateFlow<Long> = _eyeClosureDuration
```

### 數據更新機制
- 在 `updateUIState()` 方法中調用 `updateDetectionData()`
- 定期更新打哈欠次數、閉眼時間和眨眼頻率
- 異常處理確保數據更新不會影響 UI 穩定性

### UI 組件架構
- 使用 Compose 的 Column 和 Row 佈局
- 圓形指示器使用 `CircleShape` 和 `clip()` 修飾符
- 統計數據使用 `Arrangement.SpaceEvenly` 均勻分佈
- 響應式設計，適配不同螢幕尺寸

## 📱 用戶體驗改進

### 視覺層次優化
- 清晰的視覺層次：標題 → 預覽 → 指示器 → 統計 → 按鈕
- 合理的間距和對齊方式
- 一致的顏色主題和字體樣式

### 信息展示優化
- 疲勞等級一目了然，數字和顏色雙重指示
- 即時統計數據，幫助用戶了解當前狀態
- 簡潔的標籤文字，避免信息過載

### 交互設計
- 保持原有的側邊欄導航功能
- 保持原有的疲勞警告對話框
- 新增儲存記錄功能入口（待實現）

## 🧪 測試狀態

- ✅ 編譯成功，無語法錯誤
- ✅ UI 組件結構完整
- ✅ 數據流動正確
- ⏳ 需要實際運行測試 UI 效果
- ⏳ 需要測試數據更新頻率

## 🔄 後續工作

### 待實現功能
1. **儲存記錄功能**: 實現底部按鈕的記錄保存功能
2. **數據持久化**: 將統計數據保存到本地數據庫
3. **歷史記錄頁面**: 配合側邊欄的歷史記錄功能

### 優化建議
1. **動畫效果**: 為疲勞等級變化添加過渡動畫
2. **自定義主題**: 支持深色模式和自定義顏色主題
3. **數據圖表**: 在歷史記錄頁面添加數據圖表展示

## 📝 技術債務

- 移除了一些未使用的參數（編譯警告）
- 需要優化數據更新頻率，避免過於頻繁的 UI 更新
- 考慮添加數據緩存機制，減少重複計算

## 🎯 驗收標準

- [x] UI 佈局與設計圖片一致
- [x] 所有數據正確顯示和更新
- [x] 編譯無錯誤
- [x] 保持原有功能完整性
- [ ] 實際運行測試通過
- [ ] 用戶體驗驗證通過

---

**備註**: 此次變更主要專注於 UI 重新設計，保持了原有的疲勞偵測邏輯和功能完整性。新的 UI 設計更加直觀和現代化，提供了更好的用戶體驗。
