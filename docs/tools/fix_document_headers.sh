#!/bin/bash

# DrowsyGuard 文檔頭部自動修復工具
# 用法: ./fix_document_headers.sh [文檔路徑] 或 ./fix_document_headers.sh --all

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
FIXED_FILES=0
SKIPPED_FILES=0

# 獲取當前日期
CURRENT_DATETIME=$(date +"%Y-%m-%d")

# 修復單個文檔
fix_single_document() {
    local file_path=$1
    local filename=$(basename "$file_path")
    
    echo -e "${BLUE}檢查文檔: $filename${NC}"
    
    # 檢查文件是否存在
    if [ ! -f "$file_path" ]; then
        echo -e "${RED}❌ 文件不存在: $file_path${NC}"
        return 1
    fi
    
    # 檢查是否已有頭部信息
    if grep -q "^---" "$file_path"; then
        echo -e "${YELLOW}⚠️  文檔已有頭部信息，跳過${NC}"
        ((SKIPPED_FILES++))
        return 0
    fi
    
    # 提取標題
    local title=$(grep "^# " "$file_path" | head -1 | sed 's/^# //')
    if [ -z "$title" ]; then
        title="$filename"
    fi
    
    # 確定文檔類型
    local doc_type="文檔"
    local tags="文檔"
    
    if [[ $filename == ARCHITECTURE_* ]]; then
        doc_type="架構文檔"
        tags="架構,設計,系統"
    elif [[ $filename == DEVELOPMENT_* ]]; then
        doc_type="開發文檔"
        tags="開發,指南,實施"
    elif [[ $filename == CHANGELOG_* ]]; then
        doc_type="更改歷程"
        tags="更改,更新,功能"
    elif [[ $filename == STANDARDS_* ]]; then
        doc_type="標準規範"
        tags="標準,規範,指南"
    elif [[ $filename == API_* ]]; then
        doc_type="API 文檔"
        tags="API,接口,文檔"
    elif [[ $filename == README.md ]]; then
        doc_type="文檔索引"
        tags="索引,導航,文檔"
    fi
    
    # 提取版本信息
    local version="v1.0.0"
    if [[ $filename =~ v([0-9]+\.[0-9]+\.[0-9]+) ]]; then
        version="v${BASH_REMATCH[1]}"
    elif [[ $filename =~ ([0-9]{4}_[0-9]{2}_[0-9]{2}) ]]; then
        version="${BASH_REMATCH[1]}"
    fi
    
    # 創建臨時文件
    local temp_file=$(mktemp)
    
    # 添加頭部信息
    cat > "$temp_file" << EOF
---
title: "$title"
version: "$version"
created_date: "$CURRENT_DATETIME"
last_updated: "$CURRENT_DATETIME"
author: "DrowsyGuard 開發團隊"
status: "active"
tags: ["$tags"]
---

EOF
    
    # 添加原文件內容
    cat "$file_path" >> "$temp_file"
    
    # 添加底部版本信息
    if ! grep -q "版本:" "$temp_file"; then
        echo "" >> "$temp_file"
        echo "---" >> "$temp_file"
        echo "" >> "$temp_file"
        echo "**版本**: $version" >> "$temp_file"
        echo "**創建日期**: $CURRENT_DATETIME" >> "$temp_file"
        echo "**狀態**: active" >> "$temp_file"
        echo "**下一步**: 完善內容" >> "$temp_file"
    fi
    
    # 替換原文件
    mv "$temp_file" "$file_path"
    
    echo -e "${GREEN}✅ 已修復文檔頭部信息${NC}"
    ((FIXED_FILES++))
}

# 修復所有文檔
fix_all_documents() {
    echo -e "${BLUE}🔧 開始修復所有文檔...${NC}"
    echo ""
    
    # 查找所有 Markdown 文件
    local files=$(find docs -name "*.md" -type f | sort)
    
    for file in $files; do
        if [ -f "$file" ]; then
            ((TOTAL_FILES++))
            fix_single_document "$file"
            echo ""
        fi
    done
}

# 生成修復報告
generate_report() {
    echo -e "${PURPLE}📊 修復報告${NC}"
    echo "================================"
    echo -e "總文檔數: ${BLUE}$TOTAL_FILES${NC}"
    echo -e "已修復: ${GREEN}$FIXED_FILES${NC}"
    echo -e "已跳過: ${YELLOW}$SKIPPED_FILES${NC}"
    
    if [ $TOTAL_FILES -gt 0 ]; then
        local fix_rate=$((FIXED_FILES * 100 / TOTAL_FILES))
        echo -e "修復率: ${BLUE}${fix_rate}%${NC}"
    fi
    
    echo ""
    
    if [ $FIXED_FILES -gt 0 ]; then
        echo -e "${GREEN}🎉 成功修復了 $FIXED_FILES 個文檔！${NC}"
    else
        echo -e "${YELLOW}ℹ️  沒有需要修復的文檔${NC}"
    fi
}

# 主函數
main() {
    echo -e "${BLUE}🔧 DrowsyGuard 文檔頭部自動修復工具${NC}"
    echo "================================"
    echo ""
    
    if [ "$1" = "--all" ] || [ -z "$1" ]; then
        fix_all_documents
    else
        if [ -f "$1" ]; then
            fix_single_document "$1"
        else
            echo -e "${RED}錯誤: 文件不存在: $1${NC}"
            echo "用法: $0 [文檔路徑] 或 $0 --all"
            exit 1
        fi
    fi
    
    generate_report
}

# 執行主函數
main "$@" 