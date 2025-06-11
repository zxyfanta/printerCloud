@echo off
echo ========================================
echo 启动云打印小程序后端服务（带Swagger）
echo ========================================

echo.
echo 正在编译项目...
call mvn clean compile
if %errorlevel% neq 0 (
    echo 编译失败！
    pause
    exit /b 1
)

echo.
echo 正在启动服务...
echo.
echo 服务启动后，可以通过以下地址访问：
echo - 应用主页: http://localhost:8080
echo - 健康检查: http://localhost:8080/health
echo - Swagger UI: http://localhost:8080/swagger-ui/index.html
echo - OpenAPI JSON: http://localhost:8080/v3/api-docs
echo.
echo 按 Ctrl+C 停止服务
echo.

call mvn spring-boot:run

pause
