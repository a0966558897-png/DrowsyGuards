# 代碼風格和約定

## Kotlin 編碼規範
- 使用 4 空格縮進
- 類名使用 PascalCase
- 函數和變量使用 camelCase
- 常量使用 UPPER_SNAKE_CASE
- 包名使用小寫字母

## 命名約定
- **類**: `FatigueDetectionManager`
- **接口**: `FatigueUiCallback`
- **函數**: `processFaceLandmarks()`
- **變量**: `isDetectionActive`
- **常量**: `TAG = "FatigueDetectionManager"`
- **包**: `com.patrick.detection`

## 文檔和註釋
- 使用 KDoc 格式的註釋
- 公共 API 必須有文檔
- 複雜邏輯需要行內註釋
- 使用中文註釋說明業務邏輯

## 架構模式
- **MVVM**: ViewModel + StateFlow
- **模組化**: 按功能分離模組
- **依賴注入**: 使用 Hilt
- **狀態管理**: 使用狀態機模式

## 錯誤處理
- 使用 try-catch 處理異常
- 記錄詳細的錯誤日誌
- 提供用戶友好的錯誤信息

## 性能考慮
- 使用協程處理異步操作
- 避免主線程阻塞
- 合理使用緩存
- 監控內存使用