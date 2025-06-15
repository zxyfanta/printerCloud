#!/bin/bash

# 创建Java GUI管理端项目结构脚本
echo "🚀 开始创建PrinterCloud Java Admin项目结构..."

# 创建主目录
mkdir -p java-admin
cd java-admin

# 创建Maven项目结构
mkdir -p src/main/java/com/printercloud/admin/{config,controller,service,model,view,component,util}
mkdir -p src/main/resources/{fxml,css,images/icons,images/logos}
mkdir -p src/test/java/com/printercloud/admin
mkdir -p scripts
mkdir -p docs

echo "✅ 项目目录结构创建完成！"
echo "📂 项目位置: $(pwd)"
