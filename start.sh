#!/bin/bash

# 云打印小程序项目启动脚本

echo "==================================="
echo "云打印小程序项目启动脚本"
echo "==================================="

# 检查Java环境
echo "检查Java环境..."
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装Java 8或更高版本"
    exit 1
fi

# 检查Maven环境
echo "检查Maven环境..."
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请先安装Maven"
    exit 1
fi

# 检查MySQL
echo "检查MySQL连接..."
if ! command -v mysql &> /dev/null; then
    echo "警告: 未找到MySQL客户端，请确保MySQL服务已启动"
fi

# 检查Redis
echo "检查Redis连接..."
if ! command -v redis-cli &> /dev/null; then
    echo "警告: 未找到Redis客户端，请确保Redis服务已启动"
fi

# 初始化数据库
echo "是否需要初始化数据库? (y/n)"
read -r init_db
if [ "$init_db" = "y" ] || [ "$init_db" = "Y" ]; then
    echo "请输入MySQL root密码:"
    read -s mysql_password
    echo "初始化数据库..."
    mysql -u root -p"$mysql_password" < docs/database.sql
    if [ $? -eq 0 ]; then
        echo "数据库初始化成功"
    else
        echo "数据库初始化失败，请检查MySQL连接"
        exit 1
    fi
fi

# 编译后端项目
echo "编译后端项目..."
cd backend
mvn clean compile
if [ $? -ne 0 ]; then
    echo "后端项目编译失败"
    exit 1
fi

# 启动后端服务
echo "启动后端服务..."
mvn spring-boot:run &
backend_pid=$!

echo "后端服务已启动，PID: $backend_pid"
echo "API地址: http://localhost:8080/api"

# 等待后端服务启动
echo "等待后端服务启动..."
sleep 10

# 检查后端服务状态
if curl -f http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "后端服务启动成功"
else
    echo "后端服务启动失败，请检查日志"
fi

echo "==================================="
echo "项目启动完成"
echo "==================================="
echo "后端服务: http://localhost:8080/api"
echo "小程序: 请使用微信开发者工具打开 miniprogram 目录"
echo ""
echo "按 Ctrl+C 停止服务"

# 等待用户中断
wait $backend_pid
