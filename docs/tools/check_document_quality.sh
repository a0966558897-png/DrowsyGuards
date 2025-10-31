#!/bin/bash

# DrowsyGuard 文檔質量檢查工具
# 用法: ./check_document_quality.sh [文檔路徑] 或 ./check_document_quality.sh --all

set -e

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# 統計變量
TOTAL_FILES=0
PASSED_FILES=0
FAILED_FILES=0
WARNING_FILES=0

# 檢查單個文檔
check_single_document() {
    local file_path=$1
    local filename=$(basename "$file_path")
    local issues=()
    local warnings=()
    
    echo -e "${BLUE}檢查文檔: $filename${NC}"
    
    # 檢查文件是否存在
    if [ ! -f "$file_path" ]; then
        echo -e "${RED}❌ 文件不存在: $file_path${NC}"
        return 1
    fi
    
    # 檢查文件大小
    local file_size=$(wc -c < "$file_path")
    if [ "$file_size" -lt 100 ]; then
        issues+=("文件太小，可能內容不完整")
    fi
    
    # 檢查文件頭部信息
    if ! grep -q "^---" "$file_path"; then
        issues+=("缺少文檔頭部信息 (---)")
    fi
    
    # 檢查必要的前綴
    local has_prefix=false
    for prefix in "ARCHITECTURE_" "DEVELOPMENT_" "CHANGELOG_" "STANDARDS_" "API_"; do
        if [[ $filename == ${prefix}* ]]; then
            has_prefix=true
            break
        fi
    done
    
    if [ "$has_prefix" = false ]; then
        warnings+=("文件名可能不符合命名規範")
    fi
    
    # 檢查版本標識
    if ! grep -q "version:" "$file_path"; then
        issues+=("缺少版本信息")
    fi
    
    # 檢查創建日期
    if ! grep -q "created_date:" "$file_path"; then
        issues+=("缺少創建日期")
    fi
    
    # 檢查狀態
    if ! grep -q "status:" "$file_path"; then
        issues+=("缺少狀態信息")
    fi
    
    # 檢查標籤
    if ! grep -q "tags:" "$file_path"; then
        warnings+=("缺少標籤信息")
    fi
    
    # 檢查標題
    if ! grep -q "^# " "$file_path"; then
        issues+=("缺少主標題")
    fi
    
    # 檢查概述章節
    if ! grep -q "##.*概述\|##.*Overview" "$file_path"; then
        warnings+=("缺少概述章節")
    fi
    
    # 檢查總結章節
    if ! grep -q "##.*總結\|##.*Summary\|---" "$file_path"; then
        warnings+=("缺少總結或分隔線")
    fi
    
    # 檢查版本信息在底部
    if ! grep -q "版本:" "$file_path"; then
        warnings+=("底部缺少版本信息")
    fi
    
    # 檢查鏈接有效性
    local links=$(grep -o "\[.*\](.*)" "$file_path" | sed 's/.*(\([^)]*\)).*/\1/')
    for link in $links; do
        if [[ $link == http* ]]; then
            # 外部鏈接，跳過檢查
            continue
        elif [[ $link == ./* ]]; then
            # 相對鏈接，檢查文件是否存在
            local link_path=$(dirname "$file_path")/$link
            if [ ! -f "$link_path" ]; then
                warnings+=("鏈接文件不存在: $link")
            fi
        fi
    done
    
    # 輸出檢查結果
    if [ ${#issues[@]} -eq 0 ] && [ ${#warnings[@]} -eq 0 ]; then
        echo -e "${GREEN}✅ 通過所有檢查${NC}"
        ((PASSED_FILES++))
    else
        if [ ${#issues[@]} -gt 0 ]; then
            echo -e "${RED}❌ 發現問題:${NC}"
            for issue in "${issues[@]}"; do
                echo -e "  - ${RED}$issue${NC}"
            done
            ((FAILED_FILES++))
        fi
        
        if [ ${#warnings[@]} -gt 0 ]; then
            echo -e "${YELLOW}⚠️  警告:${NC}"
            for warning in "${warnings[@]}"; do
                echo -e "  - ${YELLOW}$warning${NC}"
            done
            ((WARNING_FILES++))
        fi
    fi
    
    echo ""
}

# 檢查所有文檔
check_all_documents() {
    echo -e "${BLUE}🔍 開始檢查所有文檔...${NC}"
    echo ""
    
    # 查找所有 Markdown 文件
    local files=$(find docs -name "*.md" -type f | sort)
    
    for file in $files; do
        if [ -f "$file" ]; then
            ((TOTAL_FILES++))
            check_single_document "$file"
        fi
    done
}

# 生成檢查報告
generate_report() {
    echo -e "${PURPLE}📊 檢查報告${NC}"
    echo "================================"
    echo -e "總文檔數: ${BLUE}$TOTAL_FILES${NC}"
    echo -e "通過檢查: ${GREEN}$PASSED_FILES${NC}"
    echo -e "檢查失敗: ${RED}$FAILED_FILES${NC}"
    echo -e "存在警告: ${YELLOW}$WARNING_FILES${NC}"
    
    if [ $TOTAL_FILES -gt 0 ]; then
        local pass_rate=$((PASSED_FILES * 100 / TOTAL_FILES))
        echo -e "通過率: ${BLUE}${pass_rate}%${NC}"
    fi
    
    echo ""
    
    if [ $FAILED_FILES -eq 0 ]; then
        echo -e "${GREEN}🎉 所有文檔都通過了基本檢查！${NC}"
    else
        echo -e "${RED}⚠️  請修復上述問題以確保文檔質量${NC}"
    fi
}

# 檢查文檔統計
check_statistics() {
    echo -e "${PURPLE}📈 文檔統計${NC}"
    echo "================================"
    
    local arch_count=$(find docs/architecture -name "*.md" 2>/dev/null | wc -l)
    local dev_count=$(find docs/development -name "*.md" 2>/dev/null | wc -l)
    local changelog_count=$(find docs/changelog -name "*.md" 2>/dev/null | wc -l)
    local standards_count=$(find docs/standards -name "*.md" 2>/dev/null | wc -l)
    local tools_count=$(find docs/tools -name "*.md" 2>/dev/null | wc -l)
    
    echo -e "架構文檔: ${BLUE}$arch_count${NC}"
    echo -e "開發文檔: ${BLUE}$dev_count${NC}"
    echo -e "更改歷程: ${BLUE}$changelog_count${NC}"
    echo -e "標準規範: ${BLUE}$standards_count${NC}"
    echo -e "工具文檔: ${BLUE}$tools_count${NC}"
    
    local total=$((arch_count + dev_count + changelog_count + standards_count + tools_count))
    echo -e "總計: ${BLUE}$total${NC}"
    echo ""
}

# 檢查索引文件
check_index_file() {
    echo -e "${PURPLE}🔗 檢查索引文件${NC}"
    echo "================================"
    
    local index_file="docs/README.md"
    if [ ! -f "$index_file" ]; then
        echo -e "${RED}❌ 主索引文件不存在${NC}"
        return 1
    fi
    
    local issues=0
    
    # 檢查所有文檔是否都在索引中
    local files=$(find docs -name "*.md" -type f | grep -v "README.md" | sort)
    for file in $files; do
        local filename=$(basename "$file")
        if ! grep -q "$filename" "$index_file"; then
            echo -e "${YELLOW}⚠️  文檔未在索引中: $filename${NC}"
            ((issues++))
        fi
    done
    
    if [ $issues -eq 0 ]; then
        echo -e "${GREEN}✅ 所有文檔都在索引中${NC}"
    else
        echo -e "${YELLOW}⚠️  發現 $issues 個文檔未在索引中${NC}"
    fi
    
    echo ""
}

# 主函數
main() {
    echo -e "${BLUE}🔍 DrowsyGuard 文檔質量檢查工具${NC}"
    echo "================================"
    echo ""
    
    if [ "$1" = "--all" ] || [ -z "$1" ]; then
        check_all_documents
    else
        if [ -f "$1" ]; then
            check_single_document "$1"
        else
            echo -e "${RED}錯誤: 文件不存在: $1${NC}"
            echo "用法: $0 [文檔路徑] 或 $0 --all"
            exit 1
        fi
    fi
    
    check_statistics
    check_index_file
    generate_report
}

# 執行主函數
main "$@" 