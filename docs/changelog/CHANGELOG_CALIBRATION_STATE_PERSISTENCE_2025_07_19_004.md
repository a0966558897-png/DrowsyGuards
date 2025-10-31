# 校正狀態持久化改進

**日期：** 2025-07-19  
**時間：** 004  
**版本：** 1.0.0  
**類型：** 功能改進

## 📋 問題描述

用戶反映在程式運作中，如果丟失臉部太久會重新觸發校正流程。這會導致：
- 重複的校正流程干擾用戶體驗
- 校正狀態在程式重置時丟失
- 無法確保校正只在程式啟動時執行一次

## 🔧 解決方案

### 創建持久化的校正狀態管理器

**文件：** `shared-core/src/main/java/com/patrick/core/CalibrationStateManager.kt`

**功能特點：**
- 使用 SharedPreferences 持久化保存校正狀態
- 基於會話 ID 的校正狀態管理
- 確保校正只在當前會話中有效
- 提供詳細的校正狀態信息

**核心方法：**
```kotlin
// 檢查是否已經完成校正
fun hasCalibrated(): Boolean

// 標記校正已完成
fun markCalibrationCompleted()

// 重置校正狀態（僅在程式完全關閉時調用）
fun resetCalibrationState()

// 獲取校正狀態的詳細信息
fun getCalibrationStatusInfo(): String
```

### 修改 FatigueDetector 使用持久化狀態

**文件：** `detection-logic/src/main/java/com/patrick/detection/FatigueDetector.kt`

**主要修改：**

#### 1. 構造函數修改
```kotlin
// 之前
class FatigueDetector {

// 之後
class FatigueDetector(private val context: Context) {
    private val calibrationStateManager = CalibrationStateManager(context)
```

#### 2. 校正狀態檢查修改
```kotlin
// 之前
if (!hasCalibrated && !isCalibrating && currentTime - faceDetectionStartTime >= faceDetectionDelay) {

// 之後
if (!calibrationStateManager.hasCalibrated() && !isCalibrating && currentTime - faceDetectionStartTime >= faceDetectionDelay) {
```

#### 3. 校正完成標記修改
```kotlin
// 之前
hasCalibrated = true

// 之後
calibrationStateManager.markCalibrationCompleted()
```

#### 4. 重置方法修改
```kotlin
// 之前
hasCalibrated = false // 重置校正標記

// 之後
// 注意：不重置校正狀態，使用持久化的校正狀態管理器
```

#### 5. 新增重置校正狀態方法
```kotlin
/**
 * 重置校正狀態（僅在程式完全關閉時調用）
 */
fun resetCalibrationState() {
    calibrationStateManager.resetCalibrationState()
    Log.d(TAG, "校正狀態已重置")
}
```

### 修改 FatigueDetectionManager

**文件：** `detection-logic/src/main/java/com/patrick/detection/FatigueDetectionManager.kt`

**主要修改：**

#### 1. FatigueDetector 實例化修改
```kotlin
// 之前
private val fatigueDetector = FatigueDetector()

// 之後
private val fatigueDetector = FatigueDetector(context)
```

#### 2. 新增重置校正狀態方法
```kotlin
/**
 * 重置校正狀態（僅在程式完全關閉時調用）
 */
fun resetCalibrationState() {
    fatigueDetector.resetCalibrationState()
    Log.d(TAG, "校正狀態已重置")
}
```

## 📊 校正狀態管理機制

### 會話 ID 機制

每個程式啟動時會生成唯一的會話 ID：
```kotlin
private var currentSessionId: String = generateSessionId()

private fun generateSessionId(): String {
    return System.currentTimeMillis().toString()
}
```

### 持久化存儲

使用 SharedPreferences 保存以下信息：
- `has_calibrated`: 是否已完成校正
- `calibration_timestamp`: 校正完成時間戳
- `app_session_id`: 程式會話 ID

### 校正狀態檢查邏輯

```kotlin
fun hasCalibrated(): Boolean {
    val calibrated = sharedPreferences.getBoolean(KEY_HAS_CALIBRATED, false)
    val sessionId = sharedPreferences.getString(KEY_APP_SESSION_ID, null)
    
    // 只有在當前會話中完成校正才返回 true
    return calibrated && sessionId == currentSessionId
}
```

## ✅ 功能特點

### 1. **持久化存儲**
- 校正狀態保存在 SharedPreferences 中
- 程式重啟後仍能記住校正狀態
- 不會因為程式重置而丟失

### 2. **會話隔離**
- 每個程式啟動都有唯一的會話 ID
- 校正狀態只在當前會話中有效
- 避免跨會話的狀態混淆

### 3. **智能重置**
- 提供手動重置校正狀態的方法
- 僅在程式完全關閉時調用
- 確保校正狀態的正確管理

### 4. **詳細狀態信息**
- 提供校正狀態的詳細信息
- 包含校正時間、會話 ID 等
- 便於調試和問題診斷

## 🔍 使用方式

### 檢查校正狀態

```kotlin
// 在 FatigueDetector 中
if (!calibrationStateManager.hasCalibrated()) {
    // 開始校正流程
    startCalibration()
}
```

### 標記校正完成

```kotlin
// 在校正完成時
calibrationStateManager.markCalibrationCompleted()
```

### 重置校正狀態

```kotlin
// 僅在程式完全關閉時調用
fatigueDetectionManager.resetCalibrationState()
```

### 獲取校正狀態信息

```kotlin
val statusInfo = calibrationStateManager.getCalibrationStatusInfo()
Log.d(TAG, statusInfo)
```

## 📝 日誌輸出示例

### 校正狀態檢查日誌

```
[14:30:15.123] [CalibrationStateManager] 檢查校正狀態: calibrated=true, sessionId=1734567890123, currentSessionId=1734567890123
[14:30:15.123] [CalibrationStateManager] 校正已完成並保存: timestamp=1734567890123, sessionId=1734567890123
```

### 校正狀態信息

```
校正狀態信息:
- 已校正: true
- 校正時間: 2025-07-19 14:30:15
- 會話ID: 1734567890123
- 當前會話ID: 1734567890123
- 當前會話校正: true
- 有效校正: true
```

## 🎯 預期效果

1. **避免重複校正**：程式運行期間不會重複觸發校正
2. **狀態持久化**：校正狀態在程式重置後仍能保持
3. **會話隔離**：每次程式啟動都有獨立的校正狀態
4. **用戶體驗改善**：減少不必要的校正流程干擾

## 📝 注意事項

1. **會話 ID 生成**：基於時間戳生成，確保唯一性
2. **重置時機**：僅在程式完全關閉時重置校正狀態
3. **存儲空間**：SharedPreferences 數據量很小，不會影響性能
4. **向後兼容**：新版本會自動處理舊版本的校正狀態

## 🔄 後續優化

1. **校正有效期**：可以考慮添加校正的有效期限制
2. **多用戶支持**：可以考慮支持多用戶的校正狀態
3. **校正質量評估**：可以考慮添加校正質量的評估機制
4. **自動重置策略**：可以考慮添加自動重置校正狀態的策略 