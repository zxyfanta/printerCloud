@echo off
echo ===================================================
echo 下载PrinterCloud项目依赖
echo ===================================================
echo.

echo 1. 下载后端API服务依赖...
cd /d d:\printerCloud\backend
mvn dependency:resolve
if %ERRORLEVEL% NEQ 0 (
    echo 后端依赖下载失败！
    pause
    exit /b 1
)
echo 后端依赖下载完成！
echo.

echo 2. 下载管理员GUI应用依赖...
cd /d d:\printerCloud\java-admin
mvn dependency:resolve
if %ERRORLEVEL% NEQ 0 (
    echo 管理员GUI依赖下载失败！
    pause
    exit /b 1
)
echo 管理员GUI依赖下载完成！
echo.

echo 3. 编译项目...
cd /d d:\printerCloud\backend
mvn clean compile
cd /d d:\printerCloud\java-admin
mvn clean compile
echo 编译完成！
echo.

echo ===================================================
echo 所有依赖下载完成！
echo 现在可以运行start-all.bat启动应用
echo ===================================================
pause