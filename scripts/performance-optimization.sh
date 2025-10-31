#!/bin/bash

# 性能優化腳本
# 用於分析和優化應用程序性能

set -e

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 項目根目錄
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "🚀 開始性能優化分析..."
echo "項目根目錄: $PROJECT_ROOT"

# 函數：分析內存使用
analyze_memory_usage() {
    echo -e "${BLUE}🧠 分析內存使用...${NC}"
    
    # 檢查內存洩漏
    if [ -d "build/reports" ]; then
        echo "📁 檢查內存洩漏報告..."
        find build/reports -name "*leak*" -type f 2>/dev/null || echo "未找到內存洩漏報告"
    fi
    
    # 檢查堆轉儲
    if [ -d "build/outputs" ]; then
        echo "📁 檢查堆轉儲文件..."
        find build/outputs -name "*.hprof" -type f 2>/dev/null || echo "未找到堆轉儲文件"
    fi
}

# 函數：分析 CPU 使用
analyze_cpu_usage() {
    echo -e "${BLUE}⚡ 分析 CPU 使用...${NC}"
    
    # 檢查性能分析報告
    if [ -d "build/reports" ]; then
        echo "📁 檢查性能分析報告..."
        find build/reports -name "*profile*" -type f 2>/dev/null || echo "未找到性能分析報告"
    fi
    
    # 檢查 TraceView 報告
    if [ -d "build/outputs" ]; then
        echo "📁 檢查 TraceView 報告..."
        find build/outputs -name "*.trace" -type f 2>/dev/null || echo "未找到 TraceView 報告"
    fi
}

# 函數：分析啟動時間
analyze_startup_time() {
    echo -e "${BLUE}⏱️  分析啟動時間...${NC}"
    
    # 檢查啟動時間報告
    if [ -d "build/reports" ]; then
        echo "📁 檢查啟動時間報告..."
        find build/reports -name "*startup*" -type f 2>/dev/null || echo "未找到啟動時間報告"
    fi
}

# 函數：分析電池使用
analyze_battery_usage() {
    echo -e "${BLUE}🔋 分析電池使用...${NC}"
    
    # 檢查電池使用報告
    if [ -d "build/reports" ]; then
        echo "📁 檢查電池使用報告..."
        find build/reports -name "*battery*" -type f 2>/dev/null || echo "未找到電池使用報告"
    fi
}

# 函數：生成性能報告
generate_performance_report() {
    echo -e "${BLUE}📊 生成性能報告...${NC}"
    
    local report_file="build/reports/performance_report_$(date +%Y%m%d_%H%M%S).md"
    
    mkdir -p "$(dirname "$report_file")"
    
    cat > "$report_file" << EOF
# 性能優化報告

**生成時間**: $(date)
**項目**: DrowsyGuard

## 📊 性能指標

### 內存使用
- 堆大小: 待分析
- 內存洩漏: 待檢查
- GC 頻率: 待監控

### CPU 使用
- 主線程使用率: 待分析
- 背景線程使用率: 待分析
- 熱點方法: 待識別

### 啟動時間
- 冷啟動時間: 待測量
- 熱啟動時間: 待測量
- 啟動優化建議: 待生成

### 電池使用
- 背景耗電: 待分析
- 前台耗電: 待分析
- 優化建議: 待生成

## 🔧 優化建議

### 高優先級
1. 內存優化
2. 啟動時間優化
3. 電池使用優化

### 中優先級
1. CPU 使用優化
2. 網絡請求優化
3. UI 渲染優化

### 低優先級
1. 代碼結構優化
2. 資源文件優化
3. 第三方庫優化

## 📈 改進計劃

1. **短期 (1週)**
   - 實施內存優化
   - 優化啟動流程
   - 添加性能監控

2. **中期 (1個月)**
   - 實施電池優化
   - 優化 UI 渲染
   - 添加性能測試

3. **長期 (3個月)**
   - 持續性能監控
   - 自動化性能測試
   - 性能基準建立

EOF

    echo "📄 性能報告已生成: $report_file"
}

# 函數：運行性能測試
run_performance_tests() {
    echo -e "${BLUE}🧪 運行性能測試...${NC}"
    
    # 運行基準測試
    if ./gradlew benchmark; then
        echo -e "${GREEN}✅ 基準測試通過${NC}"
    else
        echo -e "${YELLOW}⚠️  基準測試失敗或未配置${NC}"
    fi
    
    # 運行性能分析
    if ./gradlew profile; then
        echo -e "${GREEN}✅ 性能分析完成${NC}"
    else
        echo -e "${YELLOW}⚠️  性能分析失敗或未配置${NC}"
    fi
}

# 函數：檢查性能配置
check_performance_config() {
    echo -e "${BLUE}⚙️  檢查性能配置...${NC}"
    
    # 檢查 Gradle 配置
    if [ -f "config/performance/performance-config.gradle" ]; then
        echo -e "${GREEN}✅ 性能配置文件存在${NC}"
    else
        echo -e "${RED}❌ 性能配置文件缺失${NC}"
    fi
    
    # 檢查性能監控代碼
    if [ -f "shared-core/src/main/java/com/patrick/core/PerformanceMonitor.kt" ]; then
        echo -e "${GREEN}✅ 性能監控工具存在${NC}"
    else
        echo -e "${RED}❌ 性能監控工具缺失${NC}"
    fi
    
    # 檢查對象池
    if [ -f "shared-core/src/main/java/com/patrick/core/ObjectPool.kt" ]; then
        echo -e "${GREEN}✅ 對象池管理工具存在${NC}"
    else
        echo -e "${RED}❌ 對象池管理工具缺失${NC}"
    fi
}

# 函數：生成優化建議
generate_optimization_suggestions() {
    echo -e "${BLUE}💡 生成優化建議...${NC}"
    
    local suggestions_file="build/reports/optimization_suggestions_$(date +%Y%m%d_%H%M%S).md"
    
    mkdir -p "$(dirname "$suggestions_file")"
    
    cat > "$suggestions_file" << EOF
# 性能優化建議

## 🚀 啟動優化

### 1. 懶加載
- 使用 AndroidX Startup 進行組件初始化
- 將非關鍵初始化移到背景線程
- 實現組件依賴圖優化

### 2. 資源預加載
- 預加載常用字體
- 預加載音頻資源
- 使用對象池管理資源

### 3. 啟動監控
- 添加啟動時間監控
- 識別啟動瓶頸
- 建立啟動基準

## 🧠 內存優化

### 1. 對象池
- 實現 Bitmap 對象池
- 實現 ByteArray 對象池
- 實現 StringBuilder 對象池

### 2. 內存監控
- 添加內存使用監控
- 檢測內存洩漏
- 優化 GC 頻率

### 3. 資源管理
- 及時釋放 Bitmap
- 優化字符串操作
- 減少對象創建

## ⚡ CPU 優化

### 1. 異步處理
- 使用協程處理異步任務
- 實現任務優先級管理
- 優化線程池使用

### 2. 算法優化
- 優化疲勞檢測算法
- 減少不必要的計算
- 使用緩存機制

### 3. 代碼優化
- 避免在主線程進行重計算
- 優化循環和條件判斷
- 減少反射使用

## 🔋 電池優化

### 1. 後台任務
- 使用 WorkManager 管理後台任務
- 實現電池感知的任務調度
- 優化網絡請求頻率

### 2. 傳感器使用
- 優化相機使用
- 實現傳感器休眠
- 減少不必要的喚醒

### 3. 位置服務
- 優化位置請求頻率
- 使用地理圍欄
- 實現位置緩存

## 🎨 UI 優化

### 1. 渲染優化
- 減少過度繪製
- 優化佈局層次
- 使用硬件加速

### 2. 動畫優化
- 使用屬性動畫
- 優化動畫性能
- 實現動畫緩存

### 3. 列表優化
- 實現視圖回收
- 優化適配器
- 使用分頁加載

## 📊 監控和測試

### 1. 性能監控
- 實現實時性能監控
- 添加性能警報
- 建立性能基準

### 2. 自動化測試
- 添加性能測試
- 實現回歸測試
- 建立 CI/CD 流程

### 3. 用戶體驗
- 監控 ANR 和崩潰
- 優化響應時間
- 改善用戶反饋

EOF

    echo "📄 優化建議已生成: $suggestions_file"
}

# 主函數
main() {
    local action="${1:-all}"
    
    case $action in
        "memory")
            echo -e "${YELLOW}🎯 分析內存使用${NC}"
            analyze_memory_usage
            ;;
        "cpu")
            echo -e "${YELLOW}🎯 分析 CPU 使用${NC}"
            analyze_cpu_usage
            ;;
        "startup")
            echo -e "${YELLOW}🎯 分析啟動時間${NC}"
            analyze_startup_time
            ;;
        "battery")
            echo -e "${YELLOW}🎯 分析電池使用${NC}"
            analyze_battery_usage
            ;;
        "test")
            echo -e "${YELLOW}🎯 運行性能測試${NC}"
            run_performance_tests
            ;;
        "config")
            echo -e "${YELLOW}🎯 檢查性能配置${NC}"
            check_performance_config
            ;;
        "suggestions")
            echo -e "${YELLOW}🎯 生成優化建議${NC}"
            generate_optimization_suggestions
            ;;
        "report")
            echo -e "${YELLOW}🎯 生成性能報告${NC}"
            generate_performance_report
            ;;
        "all"|*)
            echo -e "${YELLOW}🎯 執行完整性能分析${NC}"
            
            check_performance_config
            analyze_memory_usage
            analyze_cpu_usage
            analyze_startup_time
            analyze_battery_usage
            run_performance_tests
            generate_optimization_suggestions
            generate_performance_report
            ;;
    esac
    
    echo -e "${GREEN}🎉 性能優化分析完成！${NC}"
}

# 顯示幫助信息
show_help() {
    echo "用法: $0 [操作]"
    echo ""
    echo "操作:"
    echo "  memory      - 分析內存使用"
    echo "  cpu         - 分析 CPU 使用"
    echo "  startup     - 分析啟動時間"
    echo "  battery     - 分析電池使用"
    echo "  test        - 運行性能測試"
    echo "  config      - 檢查性能配置"
    echo "  suggestions - 生成優化建議"
    echo "  report      - 生成性能報告"
    echo "  all         - 執行完整分析 (默認)"
    echo "  help        - 顯示此幫助信息"
    echo ""
    echo "示例:"
    echo "  $0 memory"
    echo "  $0 cpu"
    echo "  $0 all"
}

# 檢查參數
if [ "$1" = "help" ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    show_help
    exit 0
fi

# 運行主函數
main "$@" 