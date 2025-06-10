#!/bin/bash

echo "启动云打印后端服务..."

# 检查是否在正确的目录
if [ ! -f "pom.xml" ]; then
    echo "错误: 未找到pom.xml文件，请在backend目录下运行此脚本"
    exit 1
fi

# 编译项目
echo "编译项目..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "编译失败"
    exit 1
fi

# 下载依赖
echo "下载依赖..."
mvn dependency:copy-dependencies

if [ $? -ne 0 ]; then
    echo "下载依赖失败"
    exit 1
fi

# 启动服务
echo "启动服务..."
java -cp "target/classes:target/dependency/*" com.printercloud.PrinterCloudApplication

echo "服务已停止"
