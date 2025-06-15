# PowerShell script to run PrinterCloud Admin Application
Write-Host "Starting PrinterCloud Admin Application..." -ForegroundColor Green

Set-Location "d:\printerCloud\java-admin"

try {
    # Try using Maven exec plugin first
    Write-Host "Attempting to run with Maven exec plugin..." -ForegroundColor Yellow
    mvn exec:java -Dexec.mainClass="com.printercloud.admin.SwingAdminApp"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Maven exec failed, trying Spring Boot plugin..." -ForegroundColor Yellow
        mvn spring-boot:run
    }
}
catch {
    Write-Host "Error occurred: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Press any key to continue..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}