@echo off
cd /d "d:\printerCloud\java-admin"
echo Starting PrinterCloud Admin Application with Maven...
mvn exec:java -Dexec.mainClass="com.printercloud.admin.SwingAdminApp"
if %ERRORLEVEL% NEQ 0 (
    echo Application failed to start with error code %ERRORLEVEL%
    pause
)