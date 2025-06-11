@echo off
echo ========================================
echo 验证云打印小程序后端测试环境配置
echo ========================================

echo.
echo 1. 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: Java未安装或未配置环境变量
    pause
    exit /b 1
)

echo.
echo 2. 检查Maven环境...
mvn -version
if %errorlevel% neq 0 (
    echo 错误: Maven未安装或未配置环境变量
    pause
    exit /b 1
)

echo.
echo 3. 检查项目结构...
if not exist "pom.xml" (
    echo 错误: 未找到pom.xml文件，请在backend目录下运行此脚本
    pause
    exit /b 1
)

if not exist "src\main\java\com\printercloud\PrinterCloudApplication.java" (
    echo 错误: 未找到主应用类
    pause
    exit /b 1
)

if not exist "src\test\java\com\printercloud\PrinterCloudApplicationTests.java" (
    echo 错误: 未找到测试类
    pause
    exit /b 1
)

echo.
echo 4. 验证依赖配置...
call mvn dependency:resolve -q
if %errorlevel% neq 0 (
    echo 错误: Maven依赖解析失败
    pause
    exit /b 1
)

echo.
echo 5. 编译测试...
call mvn clean compile test-compile -q
if %errorlevel% neq 0 (
    echo 错误: 编译失败
    pause
    exit /b 1
)

echo.
echo 6. 运行基础测试...
call mvn test -Dtest=PrinterCloudApplicationTests -q
if %errorlevel% neq 0 (
    echo 错误: 基础测试失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo 验证完成！环境配置正确
echo ========================================
echo.
echo 可用的命令：
echo - 运行所有测试: mvn test
echo - 启动应用: mvn spring-boot:run
echo - 生成覆盖率报告: mvn test jacoco:report
echo.
echo Swagger UI地址: http://localhost:8080/swagger-ui/index.html
echo 健康检查地址: http://localhost:8080/health
echo.

pause
