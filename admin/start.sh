#!/bin/bash

echo "🖨️ 启动云打印管理后台..."

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo "❌ 错误: 未找到Node.js，请先安装Node.js 16+"
    exit 1
fi

# 检查npm是否安装
if ! command -v npm &> /dev/null; then
    echo "❌ 错误: 未找到npm"
    exit 1
fi

# 检查是否在正确的目录
if [ ! -f "package.json" ]; then
    echo "❌ 错误: 未找到package.json文件，请在admin目录下运行此脚本"
    exit 1
fi

# 检查依赖是否已安装
if [ ! -d "node_modules" ]; then
    echo "📦 安装依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ 依赖安装失败"
        exit 1
    fi
fi

# 检查后端服务是否运行
echo "🔍 检查后端服务..."
if curl -f http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "✅ 后端服务运行正常"
else
    echo "⚠️  警告: 后端服务未运行，请先启动后端服务"
    echo "   在backend目录下运行: mvn spring-boot:run"
    echo ""
    read -p "是否继续启动前端？(y/n): " continue_start
    if [ "$continue_start" != "y" ] && [ "$continue_start" != "Y" ]; then
        exit 1
    fi
fi

# 启动开发服务器
echo "🚀 启动开发服务器..."
npm run dev

echo "管理后台已停止"
