#!/bin/bash

# 測試運行腳本
# 用於運行不同類型的測試

set -e

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 項目根目錄
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "🔬 開始測試運行..."
echo "項目根目錄: $PROJECT_ROOT"

# 函數：運行單元測試
run_unit_tests() {
    echo -e "${BLUE}🧪 運行單元測試...${NC}"
    
    if ./gradlew test; then
        echo -e "${GREEN}✅ 單元測試通過${NC}"
        return 0
    else
        echo -e "${RED}❌ 單元測試失敗${NC}"
        return 1
    fi
}

# 函數：運行集成測試
run_integration_tests() {
    echo -e "${BLUE}🔗 運行集成測試...${NC}"
    
    if ./gradlew connectedAndroidTest; then
        echo -e "${GREEN}✅ 集成測試通過${NC}"
        return 0
    else
        echo -e "${RED}❌ 集成測試失敗${NC}"
        return 1
    fi
}

# 函數：運行 UI 測試
run_ui_tests() {
    echo -e "${BLUE}🎨 運行 UI 測試...${NC}"
    
    if ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.patrick.main.ui.FatigueMainScreenUITest; then
        echo -e "${GREEN}✅ UI 測試通過${NC}"
        return 0
    else
        echo -e "${RED}❌ UI 測試失敗${NC}"
        return 1
    fi
}

# 函數：生成測試覆蓋率報告
generate_coverage_report() {
    echo -e "${BLUE}📊 生成測試覆蓋率報告...${NC}"
    
    if ./gradlew koverReport; then
        echo -e "${GREEN}✅ 測試覆蓋率報告生成成功${NC}"
        echo "📁 報告位置: build/reports/kover/"
        return 0
    else
        echo -e "${RED}❌ 測試覆蓋率報告生成失敗${NC}"
        return 1
    fi
}

# 函數：顯示測試結果摘要
show_test_summary() {
    echo -e "${BLUE}📋 測試結果摘要...${NC}"
    
    # 檢查測試報告
    if [ -d "build/reports/tests" ]; then
        echo "📁 單元測試報告: build/reports/tests/"
    fi
    
    if [ -d "build/reports/androidTests" ]; then
        echo "📁 集成測試報告: build/reports/androidTests/"
    fi
    
    if [ -d "build/reports/kover" ]; then
        echo "📁 覆蓋率報告: build/reports/kover/"
    fi
}

# 主函數
main() {
    local test_type="${1:-all}"
    local success_count=0
    local total_count=0
    
    case $test_type in
        "unit")
            echo -e "${YELLOW}🎯 運行單元測試${NC}"
            run_unit_tests && ((success_count++))
            ((total_count++))
            ;;
        "integration")
            echo -e "${YELLOW}🎯 運行集成測試${NC}"
            run_integration_tests && ((success_count++))
            ((total_count++))
            ;;
        "ui")
            echo -e "${YELLOW}🎯 運行 UI 測試${NC}"
            run_ui_tests && ((success_count++))
            ((total_count++))
            ;;
        "coverage")
            echo -e "${YELLOW}🎯 生成測試覆蓋率報告${NC}"
            generate_coverage_report && ((success_count++))
            ((total_count++))
            ;;
        "all"|*)
            echo -e "${YELLOW}🎯 運行所有測試${NC}"
            
            # 運行單元測試
            run_unit_tests && ((success_count++))
            ((total_count++))
            
            # 運行集成測試
            run_integration_tests && ((success_count++))
            ((total_count++))
            
            # 運行 UI 測試
            run_ui_tests && ((success_count++))
            ((total_count++))
            
            # 生成覆蓋率報告
            generate_coverage_report && ((success_count++))
            ((total_count++))
            ;;
    esac
    
    # 顯示結果摘要
    show_test_summary
    
    # 顯示最終結果
    echo -e "${BLUE}📊 測試結果統計:${NC}"
    echo "成功: $success_count/$total_count"
    
    if [ $success_count -eq $total_count ]; then
        echo -e "${GREEN}🎉 所有測試通過！${NC}"
        exit 0
    else
        echo -e "${RED}⚠️  部分測試失敗${NC}"
        exit 1
    fi
}

# 顯示幫助信息
show_help() {
    echo "用法: $0 [測試類型]"
    echo ""
    echo "測試類型:"
    echo "  unit        - 運行單元測試"
    echo "  integration - 運行集成測試"
    echo "  ui          - 運行 UI 測試"
    echo "  coverage    - 生成測試覆蓋率報告"
    echo "  all         - 運行所有測試 (默認)"
    echo "  help        - 顯示此幫助信息"
    echo ""
    echo "示例:"
    echo "  $0 unit"
    echo "  $0 integration"
    echo "  $0 all"
}

# 檢查參數
if [ "$1" = "help" ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    show_help
    exit 0
fi

# 運行主函數
main "$@" 