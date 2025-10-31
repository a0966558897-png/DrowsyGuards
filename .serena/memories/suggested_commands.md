# 建議的命令

## 基本開發命令
```bash
# 構建項目
./gradlew build

# 清理構建
./gradlew clean

# 運行測試
./gradlew test

# 運行 Android 測試
./gradlew connectedAndroidTest

# 安裝到設備
./gradlew installDebug

# 運行應用
./gradlew runDebug
```

## 代碼質量檢查
```bash
# 運行 Detekt 靜態分析
./gradlew detekt

# 運行 KtLint 格式化檢查
./gradlew ktlintCheck

# 運行 KtLint 格式化
./gradlew ktlintFormat

# 生成測試覆蓋率報告
./gradlew koverReport
```

## 系統工具
```bash
# 查看文件
ls -la

# 切換目錄
cd <directory>

# 搜索文件
find . -name "*.kt"

# 搜索文本
grep -r "pattern" .

# Git 操作
git status
git add .
git commit -m "message"
git push
```

## 調試命令
```bash
# 查看日誌
adb logcat | grep "DrowsyGuard"

# 查看設備
adb devices

# 重啟 ADB
adb kill-server && adb start-server
```

## 性能監控
```bash
# 查看內存使用
adb shell dumpsys meminfo com.patrick.drowsyguard

# 查看 CPU 使用
adb shell top | grep "drowsyguard"
```