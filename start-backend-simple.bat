@echo off
echo ========================================
echo 启动云打印后端服务（简化版）
echo ========================================

cd /d "%~dp0backend"

echo 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误：未找到Java环境，请安装Java 8或更高版本
    pause
    exit /b 1
)

echo.
echo 尝试使用Maven启动...
echo 如果Maven未安装，请手动下载并安装Maven
echo.

echo 清理并编译项目...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo Maven编译失败，请检查Maven是否正确安装
    echo.
    echo 请手动执行以下步骤：
    echo 1. 安装Maven: https://maven.apache.org/download.cgi
    echo 2. 配置环境变量
    echo 3. 重新运行此脚本
    pause
    exit /b 1
)

echo 启动Spring Boot应用...
echo 服务将在 http://localhost:8081 启动
echo WebSocket端点: http://localhost:8081/api/ws
echo 健康检查: http://localhost:8081/api/health
echo.
echo 按Ctrl+C停止服务
echo.

call mvn spring-boot:run

echo.
echo 服务已停止
pause
