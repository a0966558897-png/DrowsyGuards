#!/bin/bash

# 代碼質量檢查腳本
# 用於檢查整個項目的代碼質量

set -e

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 項目根目錄
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo -e "${BLUE}🔍 開始代碼質量檢查...${NC}"
echo "項目根目錄: $PROJECT_ROOT"
echo ""

# 檢查函數
check_detekt() {
    echo -e "${BLUE}📊 運行 Detekt 靜態代碼分析...${NC}"
    if ./gradlew detekt; then
        echo -e "${GREEN}✅ Detekt 檢查通過${NC}"
        return 0
    else
        echo -e "${RED}❌ Detekt 檢查失敗${NC}"
        return 1
    fi
}

check_ktlint() {
    echo -e "${BLUE}🎨 運行 ktlint 代碼格式化檢查...${NC}"
    if ./gradlew ktlintCheck; then
        echo -e "${GREEN}✅ ktlint 檢查通過${NC}"
        return 0
    else
        echo -e "${RED}❌ ktlint 檢查失敗${NC}"
        return 1
    fi
}

fix_ktlint() {
    echo -e "${BLUE}🔧 自動修復 ktlint 問題...${NC}"
    if ./gradlew ktlintFormat; then
        echo -e "${GREEN}✅ ktlint 自動修復完成${NC}"
        return 0
    else
        echo -e "${RED}❌ ktlint 自動修復失敗${NC}"
        return 1
    fi
}

check_build() {
    echo -e "${BLUE}🔨 檢查項目構建...${NC}"
    if ./gradlew assembleDebug; then
        echo -e "${GREEN}✅ 項目構建成功${NC}"
        return 0
    else
        echo -e "${RED}❌ 項目構建失敗${NC}"
        return 1
    fi
}

generate_reports() {
    echo -e "${BLUE}📋 生成代碼質量報告...${NC}"
    
    # 創建報告目錄
    mkdir -p "$PROJECT_ROOT/reports"
    
    # 生成 Detekt 報告
    ./gradlew detekt > "$PROJECT_ROOT/reports/detekt-report.txt" 2>&1 || true
    
    # 生成 ktlint 報告
    ./gradlew ktlintCheck > "$PROJECT_ROOT/reports/ktlint-report.txt" 2>&1 || true
    
    echo -e "${GREEN}✅ 報告已生成到 reports/ 目錄${NC}"
}

# 主函數
main() {
    local total_checks=0
    local passed_checks=0
    local failed_checks=0
    
    # 檢查構建
    ((total_checks++))
    if check_build; then
        ((passed_checks++))
    else
        ((failed_checks++))
    fi
    
    # 檢查 ktlint
    ((total_checks++))
    if check_ktlint; then
        ((passed_checks++))
    else
        ((failed_checks++))
        echo -e "${YELLOW}💡 嘗試自動修復 ktlint 問題...${NC}"
        if fix_ktlint; then
            echo -e "${YELLOW}💡 重新檢查 ktlint...${NC}"
            if check_ktlint; then
                ((passed_checks++))
                ((failed_checks--))
            fi
        fi
    fi
    
    # 檢查 Detekt
    ((total_checks++))
    if check_detekt; then
        ((passed_checks++))
    else
        ((failed_checks++))
    fi
    
    # 生成報告
    generate_reports
    
    # 總結
    echo ""
    echo -e "${BLUE}📊 檢查結果總結:${NC}"
    echo -e "總檢查數: ${total_checks}"
    echo -e "通過: ${GREEN}${passed_checks}${NC}"
    echo -e "失敗: ${RED}${failed_checks}${NC}"
    
    if [ $failed_checks -eq 0 ]; then
        echo -e "${GREEN}🎉 所有檢查都通過了！${NC}"
        exit 0
    else
        echo -e "${RED}⚠️  有 ${failed_checks} 個檢查失敗，請查看報告${NC}"
        exit 1
    fi
}

# 執行主函數
main "$@" 