# 偵測邏輯修改總結

## 概述
根據您的需求，我已經重新修改了偵測邏輯，讓校正只在偵測到臉部時才開始，並且根據不同的偵測狀態顯示對應的 UI 行為。

## 主要修改內容

### 1. 臉部偵測狀態管理

#### 修改文件：`detection-logic/src/main/java/com/patrick/detection/FatigueDetector.kt`

**新增功能：**
- 添加臉部偵測狀態追蹤 (`isFaceDetected`)
- 添加臉部偵測開始時間 (`faceDetectionStartTime`)
- 添加臉部偵測延遲時間 (`faceDetectionDelay = 1000L`)

**核心邏輯：**
```kotlin
private fun updateFaceDetectionState(hasFace: Boolean, currentTime: Long) {
    when {
        hasFace && !isFaceDetected -> {
            // 首次偵測到臉部
            isFaceDetected = true
            faceDetectionStartTime = currentTime
            Log.d(TAG, "首次偵測到臉部，準備開始校正")
        }
        hasFace && isFaceDetected -> {
            // 持續偵測到臉部，檢查是否應該開始校正
            if (!isCalibrating && currentTime - faceDetectionStartTime >= faceDetectionDelay) {
                Log.d(TAG, "臉部偵測穩定，開始校正流程")
                startCalibration()
            }
        }
        !hasFace && isFaceDetected -> {
            // 失去臉部偵測
            isFaceDetected = false
            if (isCalibrating) {
                Log.d(TAG, "失去臉部偵測，停止校正")
                stopCalibration()
            }
            Log.d(TAG, "失去臉部偵測")
        }
    }
}
```

### 2. 更新偵測結果模型

#### 修改文件：`shared-core/src/main/java/com/patrick/core/FatigueModels.kt`

**新增字段：**
- `faceDetected: Boolean = true` - 表示是否偵測到臉部

**更新接口：**
- 重新定義 `FatigueUiCallback` 接口，添加新的回調方法
- 刪除舊的 `FatigueUiCallback.kt` 文件

### 3. 更新疲勞偵測管理器

#### 修改文件：`detection-logic/src/main/java/com/patrick/detection/FatigueDetectionManager.kt`

**新增功能：**
- 臉部偵測狀態管理
- 根據不同狀態觸發對應的 UI 回調

**警報處理邏輯：**
```kotlin
private fun handleAlerts(result: FatigueDetectionResult) {
    if (!result.faceDetected) {
        // 沒有偵測到臉部，不處理疲勞警報
        return
    }

    if (result.isFatigueDetected) {
        when (result.fatigueLevel) {
            FatigueLevel.NOTICE -> {
                // 疲勞行為首次發生，顯示提醒
                uiCallback.onNoticeFatigue()
            }
            FatigueLevel.WARNING -> {
                // 疲勞行為重複出現或閉眼 ≥ 1.5 秒，顯示警告對話框
                uiCallback.onWarningFatigue()
                alertManager.handleFatigueDetection(result)
            }
            else -> {
                // 正常狀態
                uiCallback.onNormalDetection()
            }
        }
    } else {
        // 若無疲勞偵測，通知 UI 顯示「持續偵測中…」
        uiCallback.onNormalDetection()
    }
}
```

### 4. 更新相機 ViewModel

#### 修改文件：`camera-input/src/main/java/com/patrick/camera/CameraViewModel.kt`

**移除功能：**
- 移除自動開始校正的邏輯

**新增狀態：**
- `_isFaceDetected` - 臉部偵測狀態
- `_statusText` - 狀態文字管理

**UI 回調實現：**
```kotlin
override fun onNoFaceDetected() {
    _fatigueLevel.value = FatigueLevel.NORMAL
    _showFatigueDialog.value = false
    _isFaceDetected.value = false
    _statusText.value = "請面對鏡頭"
}

override fun onNormalDetection() {
    _fatigueLevel.value = FatigueLevel.NORMAL
    _showFatigueDialog.value = false
    _isFaceDetected.value = true
    if (!_isCalibrating.value) {
        _statusText.value = "持續偵測中…"
    }
}

override fun onNoticeFatigue() {
    _fatigueLevel.value = FatigueLevel.NOTICE
    _showFatigueDialog.value = false
    _isFaceDetected.value = true
    _statusText.value = "提醒"
}

override fun onWarningFatigue() {
    _fatigueLevel.value = FatigueLevel.WARNING
    _showFatigueDialog.value = true
    _isFaceDetected.value = true
    _statusText.value = "警告"
}
```

### 5. 更新 UI 組件

#### 修改文件：`app/src/main/java/com/patrick/main/ui/FatigueMainScreen.kt`

**更新：**
- 移除本地狀態文字計算
- 使用 CameraViewModel 提供的狀態文字

#### 修改文件：`app/src/main/java/com/patrick/main/MainActivity.kt`

**更新：**
- 收集並傳遞狀態文字到 UI 組件

## 偵測狀態對應表

| 偵測結果情境 | 對應 UI 行為 | 顯示方式 |
|-------------|-------------|----------|
| ✅ 一切正常 | 顯示「持續偵測中…」 | 導航欄中心文字提示 |
| 📵 無法偵測到臉部 | 顯示「請面對鏡頭」 | 導航欄中心文字提示 |
| ⚠️ 疲勞行為首次發生（如：打哈欠、眨眼頻率過高） | 顯示「提醒」 | 導航欄中心顯示提醒訊息 |
| 🛑 疲勞行為重複出現 或 閉眼 ≥ 1.5 秒 | 顯示「警告」 | 彈出 Dialog 要求使用者確認 |

## 校正流程

### 新的校正觸發邏輯：
1. **程式啟動** → 啟動疲勞偵測，但不開始校正
2. **偵測到臉部** → 等待 1 秒穩定偵測
3. **臉部偵測穩定** → 自動開始 15 秒校正流程
4. **失去臉部偵測** → 立即停止校正
5. **重新偵測到臉部** → 重新開始校正流程

### 校正狀態顯示：
- **校正中** → "正在校正中... X%"
- **校正完成** → "持續偵測中…"

## 技術特點

### 1. 智能校正觸發
- 只在偵測到臉部時才開始校正
- 避免了一開始沒有臉部後來才有臉部但已經過了校正時間的問題
- 支持動態臉部偵測狀態變化

### 2. 狀態管理
- 完整的臉部偵測狀態追蹤
- 清晰的疲勞級別管理
- 統一的狀態文字管理

### 3. UI 響應
- 實時狀態文字更新
- 根據不同狀態顯示對應的 UI 行為
- 支持對話框和文字提示的組合使用

## 編譯狀態

✅ **所有模組編譯成功：**
- `shared-core`: 核心模型和接口
- `detection-logic`: 疲勞偵測邏輯
- `camera-input`: 相機和 UI 管理
- `app`: 主應用模組

## 總結

這次修改成功解決了您提到的問題：

1. **✅ 校正時機優化** - 校正現在只在偵測到臉部時才開始
2. **✅ 狀態顯示完善** - 根據不同偵測狀態顯示對應的 UI 行為
3. **✅ 臉部偵測處理** - 正確處理無法偵測到臉部的情況
4. **✅ 疲勞警報分級** - 實現了提醒和警告的不同處理方式

現在應用程式會根據實際的臉部偵測狀態和疲勞程度，智能地顯示對應的提示信息，提供更好的用戶體驗。 