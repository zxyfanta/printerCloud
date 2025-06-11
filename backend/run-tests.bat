@echo off
echo ========================================
echo 云打印小程序后端测试脚本
echo ========================================

echo.
echo 1. 编译项目...
call mvn clean compile
if %errorlevel% neq 0 (
    echo 编译失败！
    pause
    exit /b 1
)

echo.
echo 2. 运行单元测试...
call mvn test -Dtest="!**/integration/**"
if %errorlevel% neq 0 (
    echo 单元测试失败！
    pause
    exit /b 1
)

echo.
echo 3. 运行集成测试...
call mvn test -Dtest="**/integration/**"
if %errorlevel% neq 0 (
    echo 集成测试失败！
    pause
    exit /b 1
)

echo.
echo 4. 生成测试覆盖率报告...
call mvn jacoco:report
if %errorlevel% neq 0 (
    echo 覆盖率报告生成失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo 测试完成！
echo ========================================
echo.
echo 测试报告位置：
echo - Surefire报告: target\surefire-reports\
echo - Failsafe报告: target\failsafe-reports\
echo - 覆盖率报告: target\site\jacoco\index.html
echo.
echo 要查看覆盖率报告，请打开: target\site\jacoco\index.html
echo.

pause
