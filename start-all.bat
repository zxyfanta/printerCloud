@echo off
echo Starting PrinterCloud Services...
echo.

echo Starting Backend API Server...
start "Backend API" cmd /k "cd /d d:\printerCloud\backend && mvn spring-boot:run"

echo Waiting for backend to start...
timeout /t 15 /nobreak > nul

echo Starting Admin GUI Application...
start "Admin GUI" cmd /k "cd /d d:\printerCloud\java-admin && mvn exec:java -Dexec.mainClass=com.printercloud.admin.SwingAdminApp"

echo.
echo Both services are starting...
echo Backend API will be available at: http://localhost:8082
echo Admin GUI should open automatically
echo.
echo Press any key to exit this window...
pause > nul