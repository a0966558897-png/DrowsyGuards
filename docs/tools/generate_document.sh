#!/bin/bash

# DrowsyGuard 通用文檔生成工具
# 用法: ./generate_document.sh [文檔類型] [文檔名稱] [描述] [版本類型]

set -e

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# 獲取當前日期和時間
CURRENT_DATE=$(date +"%Y_%m_%d")
CURRENT_TIME=$(date +"%H%M")
CURRENT_DATETIME=$(date +"%Y-%m-%d")

# 參數解析
DOC_TYPE=$1
DOC_NAME=$2
DESCRIPTION=$3
VERSION_TYPE=$4

# 參數驗證
if [ -z "$DOC_TYPE" ] || [ -z "$DOC_NAME" ] || [ -z "$DESCRIPTION" ]; then
    echo -e "${RED}錯誤: 請提供所有必要參數${NC}"
    echo "用法: $0 [文檔類型] [文檔名稱] [描述] [版本類型]"
    echo ""
    echo "文檔類型:"
    echo "  architecture  - 架構文檔"
    echo "  development   - 開發文檔"
    echo "  changelog     - 更改歷程"
    echo "  standards     - 標準規範"
    echo "  api           - API 文檔"
    echo ""
    echo "版本類型:"
    echo "  semantic      - 語義化版本 (v1.0.0)"
    echo "  timestamp     - 時間戳版本 (2025_07_19_001)"
    echo "  precise       - 精確時間戳 (2025_07_19_1430)"
    echo ""
    echo "示例:"
    echo "  $0 changelog 'UI_REFACTOR' 'UI 重構' timestamp"
    echo "  $0 architecture 'CLEAN_ARCH' 'Clean Architecture' semantic"
    echo "  $0 changelog 'SECURITY_FIX' '安全修復' precise"
    exit 1
fi

# 設置默認版本類型
if [ -z "$VERSION_TYPE" ]; then
    case $DOC_TYPE in
        "architecture"|"standards"|"api")
            VERSION_TYPE="semantic"
            ;;
        "changelog")
            VERSION_TYPE="timestamp"
            ;;
        *)
            VERSION_TYPE="timestamp"
            ;;
    esac
fi

# 生成版本標識
generate_version() {
    local type=$1
    case $type in
        "semantic")
            echo "v1.0.0"
            ;;
        "timestamp")
            # 查找當天已有的文檔，生成序列號
            local dir="docs/$DOC_TYPE"
            local seq_num=1
            
            for file in "$dir"/*_"$CURRENT_DATE"*.md; do
                if [ -f "$file" ]; then
                    local filename=$(basename "$file")
                    if [[ $filename =~ ${CURRENT_DATE}_([0-9]{3}) ]]; then
                        local existing_seq=${BASH_REMATCH[1]}
                        # 使用十進制比較
                        if [ "$((10#$existing_seq))" -ge "$seq_num" ]; then
                            seq_num=$((10#$existing_seq + 1))
                        fi
                    fi
                fi
            done
            
            local seq_formatted=$(printf "%03d" $seq_num)
            echo "${CURRENT_DATE}_${seq_formatted}"
            ;;
        "precise")
            echo "${CURRENT_DATE}_${CURRENT_TIME}"
            ;;
        *)
            echo "v1.0.0"
            ;;
    esac
}

# 生成文件名
VERSION=$(generate_version "$VERSION_TYPE")
FILENAME="$(echo $DOC_TYPE | tr '[:lower:]' '[:upper:]')_${DOC_NAME}_${VERSION}.md"
FILEPATH="docs/$DOC_TYPE/$FILENAME"

# 確保目錄存在
mkdir -p "docs/$DOC_TYPE"

echo -e "${BLUE}正在生成文檔...${NC}"
echo -e "${GREEN}文檔類型: $DOC_TYPE${NC}"
echo -e "${GREEN}文檔名稱: $DOC_NAME${NC}"
echo -e "${GREEN}描述: $DESCRIPTION${NC}"
echo -e "${GREEN}版本類型: $VERSION_TYPE${NC}"
echo -e "${GREEN}版本標識: $VERSION${NC}"
echo -e "${GREEN}文件名: $FILENAME${NC}"

# 根據文檔類型生成不同的模板
generate_template() {
    local type=$1
    local name=$2
    local desc=$3
    local version=$4
    
    case $type in
        "architecture")
            cat << EOF
# $desc

---
title: "$desc"
version: "$version"
created_date: "$CURRENT_DATETIME"
last_updated: "$CURRENT_DATETIME"
author: "DrowsyGuard 開發團隊"
status: "active"
tags: ["架構", "設計", "系統"]
---

## 🎯 概述

本文檔描述了 $desc 的架構設計和實現方案。

## 🏗️ 架構設計

### 1. 整體架構
描述整體架構設計思路

### 2. 核心組件
- 組件1: 描述
- 組件2: 描述
- 組件3: 描述

### 3. 數據流
描述數據在系統中的流動

## 🔧 技術實現

### 1. 技術選型
- 技術1: 選擇原因
- 技術2: 選擇原因
- 技術3: 選擇原因

### 2. 依賴關係
描述組件間的依賴關係

### 3. 接口設計
描述對外接口設計

## 📊 性能考慮

### 1. 性能指標
- 響應時間: 目標值
- 吞吐量: 目標值
- 資源使用: 目標值

### 2. 優化策略
- 策略1: 描述
- 策略2: 描述
- 策略3: 描述

## 🔄 部署方案

### 1. 環境要求
- 硬件要求
- 軟件要求
- 網絡要求

### 2. 部署步驟
1. 步驟1
2. 步驟2
3. 步驟3

## 📝 維護指南

### 1. 監控指標
- 指標1: 描述
- 指標2: 描述
- 指標3: 描述

### 2. 故障處理
- 故障1: 處理方法
- 故障2: 處理方法
- 故障3: 處理方法

---

**版本**: $version  
**創建日期**: $CURRENT_DATETIME  
**狀態**: active  
**下一步**: 實施和測試
EOF
            ;;
        "changelog")
            cat << EOF
# 更改歷程 - $desc - $CURRENT_DATETIME

---
title: "$desc 更改歷程"
version: "$version"
created_date: "$CURRENT_DATETIME"
last_updated: "$CURRENT_DATETIME"
author: "DrowsyGuard 開發團隊"
status: "active"
tags: ["更改", "更新", "功能"]
---

## 🎯 更改概述

本次更改實現了 $desc 功能。

## ✅ 完成的功能

### 主要功能 ✅
- [ ] 功能1
- [ ] 功能2
- [ ] 功能3

### 技術改進 ✅
- [ ] 改進1
- [ ] 改進2
- [ ] 改進3

## 🔧 技術實現

### 1. 架構設計
描述架構設計思路和實現方案

### 2. 核心技術
- 技術1: 描述
- 技術2: 描述
- 技術3: 描述

### 3. 依賴管理
- 新增依賴: 描述
- 更新依賴: 描述
- 移除依賴: 描述

## 🧪 測試結果

### 編譯測試 ✅
- [ ] 模組編譯成功
- [ ] 依賴關係正確
- [ ] 無編譯錯誤

### 功能測試 ✅
- [ ] 核心功能正常
- [ ] 邊界情況處理
- [ ] 錯誤處理完善

### 性能測試 ✅
- [ ] 響應時間達標
- [ ] 內存使用優化
- [ ] 電池消耗合理

## 📊 性能指標

### 功能指標
| 指標 | 實現前 | 實現後 | 改進幅度 |
|------|--------|--------|----------|
| 功能完整性 | 0% | 100% | **完全實現** |
| 用戶體驗 | 基準 | 提升 | **顯著改善** |
| 系統穩定性 | 基準 | 提升 | **顯著改善** |

### 技術指標
| 指標 | 實現前 | 實現後 | 改進幅度 |
|------|--------|--------|----------|
| 代碼質量 | 基準 | 提升 | **顯著改善** |
| 可維護性 | 基準 | 提升 | **顯著改善** |
| 可擴展性 | 基準 | 提升 | **顯著改善** |

## 🔄 下一步計劃

### 短期計劃 (1-2週)
1. **功能完善**
   - 補充缺失功能
   - 優化用戶體驗
   - 完善錯誤處理

2. **測試覆蓋**
   - 單元測試
   - 集成測試
   - 用戶驗收測試

### 中期計劃 (1個月)
1. **性能優化**
   - 響應速度優化
   - 內存使用優化
   - 電池消耗優化

2. **功能擴展**
   - 新功能開發
   - 現有功能增強
   - 用戶反饋整合

### 長期計劃 (3個月)
1. **生態系統建設**
   - 文檔完善
   - 開發者工具
   - 社區建設

## 📝 備註

### 重要決策
1. 決策1: 描述和原因
2. 決策2: 描述和原因
3. 決策3: 描述和原因

### 技術債務
1. 債務1: 描述和影響
2. 債務2: 描述和影響
3. 債務3: 描述和影響

### 風險緩解
1. 風險1: 描述和緩解措施
2. 風險2: 描述和緩解措施
3. 風險3: 描述和緩解措施

## 🎉 總結

本次 $desc 功能實現成功達成了預期目標：

1. **功能完整性** - 所有計劃功能都已實現
2. **技術質量** - 代碼質量和架構設計符合標準
3. **用戶體驗** - 用戶體驗得到顯著改善
4. **系統穩定性** - 系統穩定性和性能得到提升

該功能的實現為項目的長期發展奠定了堅實的基礎。

---

**版本**: $version  
**創建日期**: $CURRENT_DATETIME  
**狀態**: active  
**下一步**: 完善測試和優化
EOF
            ;;
        "standards")
            cat << EOF
# $desc

---
title: "$desc"
version: "$version"
created_date: "$CURRENT_DATETIME"
last_updated: "$CURRENT_DATETIME"
author: "DrowsyGuard 開發團隊"
status: "active"
tags: ["標準", "規範", "指南"]
---

## 📋 概述

本文檔定義了 $desc 的標準和規範。

## 🎯 目標

明確的目標和範圍

## 📝 標準規範

### 1. 基本原則
- 原則1: 描述
- 原則2: 描述
- 原則3: 描述

### 2. 具體規範
- 規範1: 描述
- 規範2: 描述
- 規範3: 描述

### 3. 實施指南
- 指南1: 描述
- 指南2: 描述
- 指南3: 描述

## 📊 檢查清單

### 1. 合規檢查
- [ ] 檢查項目1
- [ ] 檢查項目2
- [ ] 檢查項目3

### 2. 質量檢查
- [ ] 質量項目1
- [ ] 質量項目2
- [ ] 質量項目3

## 🔄 維護流程

### 1. 更新流程
1. 步驟1
2. 步驟2
3. 步驟3

### 2. 審查流程
1. 審查1
2. 審查2
3. 審查3

---

**版本**: $version  
**創建日期**: $CURRENT_DATETIME  
**狀態**: active  
**下一步**: 實施和監督
EOF
            ;;
        *)
            cat << EOF
# $desc

---
title: "$desc"
version: "$version"
created_date: "$CURRENT_DATETIME"
last_updated: "$CURRENT_DATETIME"
author: "DrowsyGuard 開發團隊"
status: "active"
tags: ["文檔", "說明"]
---

## 📋 概述

本文檔描述了 $desc 的相關內容。

## 🎯 目標

明確的目標和範圍

## 📝 內容

主要內容...

## 📊 總結

總結和結論

---

**版本**: $version  
**創建日期**: $CURRENT_DATETIME  
**狀態**: active  
**下一步**: 具體行動項目
EOF
            ;;
    esac
}

# 生成文檔內容
generate_template "$DOC_TYPE" "$DOC_NAME" "$DESCRIPTION" "$VERSION" > "$FILEPATH"

echo -e "${GREEN}✅ 文檔已成功創建: $FILEPATH${NC}"

# 更新主索引文件
echo -e "${BLUE}正在更新主索引文件...${NC}"

README_FILE="docs/README.md"

# 根據文檔類型更新對應的表格
case $DOC_TYPE in
    "architecture")
        if ! grep -q "$FILENAME" "$README_FILE"; then
            sed -i.bak "/| \[ARCHITECTURE_.*\]/a\\
| [$FILENAME](./$DOC_TYPE/$FILENAME) | $VERSION | active | $DESCRIPTION |" "$README_FILE"
        fi
        ;;
    "development")
        if ! grep -q "$FILENAME" "$README_FILE"; then
            sed -i.bak "/| \[DEVELOPMENT_.*\]/a\\
| [$FILENAME](./$DOC_TYPE/$FILENAME) | $VERSION | active | $DESCRIPTION |" "$README_FILE"
        fi
        ;;
    "changelog")
        if ! grep -q "$FILENAME" "$README_FILE"; then
            sed -i.bak "/| \[CHANGELOG_.*\]/a\\
| [$FILENAME](./$DOC_TYPE/$FILENAME) | $VERSION | active | $DESCRIPTION |" "$README_FILE"
        fi
        ;;
    "standards")
        if ! grep -q "$FILENAME" "$README_FILE"; then
            sed -i.bak "/| \[STANDARDS_.*\]/a\\
| [$FILENAME](./$DOC_TYPE/$FILENAME) | $VERSION | active | $DESCRIPTION |" "$README_FILE"
        fi
        ;;
esac

echo -e "${GREEN}✅ 主索引文件已更新${NC}"
echo -e "${GREEN}🎉 文檔生成完成！${NC}"
echo -e "${BLUE}📝 請編輯 $FILEPATH 文件，添加具體內容${NC}" 