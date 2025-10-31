# DrowsyGuard 項目概覽

## 項目目的
DrowsyGuard 是一個基於 MediaPipe 的疲勞檢測 Android 應用，使用面部特徵點檢測來監控用戶的疲勞狀態，並在檢測到疲勞跡象時提供警報。

## 技術棧
- **語言**: Kotlin
- **平台**: Android (min SDK 24, Android 7.0+)
- **架構**: 模組化架構，使用多個子模組
- **依賴管理**: Gradle
- **AI/ML**: MediaPipe Tasks Face Landmark Detection
- **UI**: Android Views + ViewModel
- **狀態管理**: StateFlow, Coroutines
- **代碼質量**: Detekt, KtLint, Kover

## 項目結構
```
DrowsyGuard/
├── app/                    # 主應用模組
├── ui-components/          # UI 組件模組
├── detection-logic/        # 疲勞檢測邏輯模組
├── camera-input/           # 相機輸入模組
├── user-alert/            # 用戶警報模組
├── user-settings/         # 用戶設定模組
├── account-auth/          # 帳戶認證模組
├── shared-core/           # 共享核心模組
├── config/                # 配置文件
├── docs/                  # 文檔
└── scripts/               # 腳本
```

## 核心功能
1. 實時疲勞檢測（基於 EAR/MAR 計算）
2. 多級警報系統（提醒、警告）
3. 校正流程
4. 狀態機管理
5. 用戶設定管理

## 開發環境
- Android Studio Dolphin+
- 實體 Android 設備（需要相機）
- 開發者模式啟用