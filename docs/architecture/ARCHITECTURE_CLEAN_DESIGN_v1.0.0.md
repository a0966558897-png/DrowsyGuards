# DrowsyGuard Clean Architecture 設計文檔

## 🏗️ 架構概述

DrowsyGuard 採用 Clean Architecture 設計模式，確保代碼的可維護性、可測試性和可擴展性。

## 📁 架構分層

### 1. **Presentation Layer (表現層)**
- **職責**: UI 顯示和用戶交互
- **組件**: 
  - `MainActivity` - 主界面
  - `FatigueMainScreen` - Compose UI
  - `FatigueViewModel` - UI 狀態管理

### 2. **Domain Layer (領域層)**
- **職責**: 業務邏輯和用例
- **組件**:
  - `CameraUseCase` - 攝像頭業務邏輯
  - `FatigueDetector` - 疲勞檢測邏輯
  - `FatigueAlertManager` - 警報管理邏輯

### 3. **Data Layer (數據層)**
- **職責**: 數據訪問和外部服務
- **組件**:
  - `CameraRepository` - 攝像頭數據接口
  - `CameraRepositoryImpl` - 攝像頭數據實現
  - `CameraManager` - 攝像頭管理
  - `CameraController` - 攝像頭控制

## 🔄 依賴關係

```
Presentation Layer
       ↓ (依賴)
   Domain Layer
       ↓ (依賴)
   Data Layer
```

### 依賴規則
1. **內層不依賴外層**: Domain 層不依賴 Presentation 層
2. **依賴抽象**: 通過接口進行依賴
3. **單向依賴**: 依賴關係是單向的

## 📦 模組結構

### Camera Input 模組
```
camera-input/
├── CameraRepository.kt          # 接口定義
├── CameraRepositoryImpl.kt      # 接口實現
├── CameraUseCase.kt             # 業務邏輯
├── CameraManager.kt             # 攝像頭管理
├── CameraController.kt          # 攝像頭控制
├── CameraPermissionManager.kt   # 權限管理
└── CameraModule.kt              # 依賴注入
```

### 模組職責分離

#### **CameraRepository (接口)**
- 定義攝像頭操作的契約
- 遵循依賴反轉原則

#### **CameraRepositoryImpl (實現)**
- 實現攝像頭數據訪問
- 包裝 CameraManager

#### **CameraUseCase (用例)**
- 處理攝像頭業務邏輯
- 管理攝像頭狀態
- 提供響應式狀態流

#### **CameraManager (管理)**
- 攝像頭生命週期管理
- 權限處理
- 錯誤處理

#### **CameraController (控制)**
- 具體的攝像頭操作
- CameraX 集成
- 重試邏輯

#### **CameraPermissionManager (權限)**
- 權限檢查和請求
- 權限狀態管理

## 🎯 設計原則

### 1. **單一職責原則 (SRP)**
- 每個類只有一個變化的原因
- 例如: `CameraPermissionManager` 只負責權限

### 2. **開放封閉原則 (OCP)**
- 對擴展開放，對修改封閉
- 通過接口實現擴展

### 3. **依賴反轉原則 (DIP)**
- 依賴抽象而非具體實現
- 使用接口進行依賴

### 4. **接口隔離原則 (ISP)**
- 客戶端不依賴它不需要的接口
- 接口小而專一

## 🔧 依賴注入

### CameraModule 工廠
```kotlin
object CameraModule {
    fun createCameraModule(
        context: Context,
        lifecycleOwner: LifecycleOwner
    ): CameraUseCase
}
```

### 使用方式
```kotlin
// MainActivity 中
private fun initializeCleanArchitecture() {
    cameraUseCase = CameraModule.createCameraModule(this, this)
}
```

## 📊 狀態管理

### 攝像頭狀態流
```kotlin
enum class CameraState {
    UNINITIALIZED,
    INITIALIZING,
    READY,
    ERROR,
    PERMISSION_REQUIRED
}
```

### 響應式狀態
```kotlin
val cameraState: StateFlow<CameraState>
val errorMessage: StateFlow<String?>
```

## 🧪 測試策略

### 單元測試
- **Domain Layer**: 測試 UseCase 業務邏輯
- **Data Layer**: 測試 Repository 實現

### 集成測試
- **Presentation Layer**: 測試 UI 組件
- **模組間**: 測試組件集成

### Mock 策略
- 使用接口進行 Mock
- 測試時替換具體實現

## 🚀 擴展指南

### 添加新功能
1. 在 Domain 層定義 UseCase
2. 在 Data 層實現 Repository
3. 在 Presentation 層更新 UI

### 添加新模組
1. 創建模組目錄
2. 定義接口和實現
3. 創建依賴注入工廠
4. 更新主模組依賴

## 📈 性能優化

### 內存管理
- 及時釋放資源
- 使用 WeakReference 避免內存洩漏

### 響應式編程
- 使用 StateFlow 進行狀態管理
- 避免不必要的狀態更新

### 生命週期管理
- 正確處理 Activity/Fragment 生命週期
- 避免在銷毀後執行操作

## 🔒 安全性

### 權限管理
- 最小權限原則
- 運行時權限檢查

### 錯誤處理
- 統一的錯誤處理機制
- 用戶友好的錯誤信息

## 📝 代碼規範

### 命名規範
- 類名: PascalCase
- 函數名: camelCase
- 常量: UPPER_SNAKE_CASE

### 註釋規範
- 公共 API 必須有文檔註釋
- 複雜邏輯需要行內註釋

### 代碼組織
- 相關功能放在同一模組
- 清晰的包結構

---

**版本**: 2.0.0  
**最後更新**: 2024年  
**架構師**: Patrick  
**遵循原則**: Clean Architecture + SOLID 