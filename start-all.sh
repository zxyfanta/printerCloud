#!/bin/bash

echo "🖨️ 启动云打印小程序系统..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java，请先安装Java 8+"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven，请先安装Maven 3.6+"
    exit 1
fi

# 检查Node.js环境
if ! command -v node &> /dev/null; then
    echo "⚠️  警告: 未找到Node.js，管理后台将无法启动"
    echo "   请安装Node.js 16+以使用管理后台"
fi

echo "✅ 环境检查通过"

# 检查是否在项目根目录
if [ ! -d "backend" ] || [ ! -d "miniprogram" ] || [ ! -d "admin" ]; then
    echo "❌ 错误: 请在项目根目录下运行此脚本"
    exit 1
fi

echo "📁 当前目录: $(pwd)"

# 询问启动选项
echo ""
echo "请选择启动选项:"
echo "1) 只启动后端服务"
echo "2) 启动后端 + 管理后台"
echo "3) 退出"
read -p "请输入选项 (1-3): " choice

case $choice in
    1)
        echo "启动后端服务..."
        ;;
    2)
        echo "启动后端服务和管理后台..."
        ;;
    3)
        echo "退出"
        exit 0
        ;;
    *)
        echo "无效选项，默认启动后端服务"
        choice=1
        ;;
esac

# 编译后端项目
echo "📦 编译后端项目..."
cd backend
mvn clean compile
if [ $? -ne 0 ]; then
    echo "❌ 后端项目编译失败"
    exit 1
fi

# 启动后端服务
echo "🚀 启动后端服务..."
mvn spring-boot:run &
backend_pid=$!

echo "✅ 后端服务已启动，PID: $backend_pid"
echo "📍 API地址: http://localhost:8080/api"
echo "📍 管理页面: http://localhost:8080"

# 等待后端服务启动
echo "⏳ 等待后端服务启动..."
sleep 15

# 检查后端服务状态
if curl -f http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "✅ 后端服务启动成功"
else
    echo "❌ 后端服务启动失败，请检查日志"
    kill $backend_pid 2>/dev/null
    exit 1
fi

# 如果选择启动管理后台
if [ "$choice" = "2" ]; then
    if command -v node &> /dev/null; then
        echo ""
        echo "🚀 启动管理后台..."
        cd ../admin
        
        # 检查依赖
        if [ ! -d "node_modules" ]; then
            echo "📦 安装前端依赖..."
            npm install
            if [ $? -ne 0 ]; then
                echo "❌ 前端依赖安装失败"
                kill $backend_pid 2>/dev/null
                exit 1
            fi
        fi
        
        # 启动前端开发服务器
        npm run dev &
        frontend_pid=$!
        
        echo "✅ 管理后台已启动，PID: $frontend_pid"
        echo "📍 管理后台地址: http://localhost:3000"
        echo ""
        echo "🔑 默认管理员账户:"
        echo "   管理员: admin / admin123"
        echo "   超级管理员: superadmin / super123"
        
        # 等待用户输入以停止服务
        echo ""
        echo "按 Ctrl+C 或 Enter 键停止所有服务..."
        read
        
        # 停止服务
        echo "🛑 停止服务..."
        kill $frontend_pid 2>/dev/null
        kill $backend_pid 2>/dev/null
        echo "✅ 所有服务已停止"
    else
        echo "❌ 无法启动管理后台: 未找到Node.js"
        echo ""
        echo "按 Ctrl+C 或 Enter 键停止后端服务..."
        read
        kill $backend_pid 2>/dev/null
        echo "✅ 后端服务已停止"
    fi
else
    echo ""
    echo "按 Ctrl+C 或 Enter 键停止后端服务..."
    read
    kill $backend_pid 2>/dev/null
    echo "✅ 后端服务已停止"
fi

echo ""
echo "🎉 感谢使用云打印小程序系统！"
