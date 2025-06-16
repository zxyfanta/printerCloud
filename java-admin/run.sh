#!/bin/bash

# 云打印管理系统启动脚本

echo "🚀 启动云打印管理系统..."

# 检查Java版本
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "☕ Java版本: $java_version"

# 检查是否已编译
if [ ! -d "target/classes" ]; then
    echo "📦 正在编译项目..."
    mvn clean compile -q
    if [ $? -ne 0 ]; then
        echo "❌ 编译失败"
        exit 1
    fi
    echo "✅ 编译完成"
fi

# 使用Spring Boot插件启动
echo "🖥️  启动Swing应用程序..."
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false"
