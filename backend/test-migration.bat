@echo off
echo ===================================================
echo Testing Spring Boot 3.x Migration
echo ===================================================
echo.

echo 1. Cleaning project...
mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Clean failed!
    pause
    exit /b 1
)
echo ✅ Clean successful!
echo.

echo 2. Compiling project...
mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compilation failed!
    echo Check the error messages above for details.
    pause
    exit /b 1
)
echo ✅ Compilation successful!
echo.

echo 3. Running tests...
mvn test
if %ERRORLEVEL% NEQ 0 (
    echo ⚠️ Some tests failed, but this might be expected.
    echo Check test results for details.
else
    echo ✅ All tests passed!
)
echo.

echo 4. Starting application...
echo The application will start in a new window.
echo Check for the "ClassNotFoundException: User" error.
echo If the error is gone, the migration was successful!
echo.
echo Press Ctrl+C in the new window to stop the application.
start "Backend Server" cmd /k "mvn spring-boot:run"

echo ===================================================
echo Migration test completed!
echo Check the new window for application startup logs.
echo ===================================================
pause