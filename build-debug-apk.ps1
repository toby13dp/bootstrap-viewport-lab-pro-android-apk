$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host ""
Write-Host "Bootstrap Viewport Lab Pro APK build" -ForegroundColor Magenta
Write-Host "Package: be.creatieplezier.viewportlab" -ForegroundColor Cyan
Write-Host "Localhost: http://127.0.0.1:53847" -ForegroundColor Cyan
Write-Host ""

if (Test-Path ".\gradlew.bat") {
    .\gradlew.bat assembleDebug
}
elseif (Get-Command gradle -ErrorAction SilentlyContinue) {
    gradle assembleDebug
}
else {
    Write-Host "Gradle niet gevonden." -ForegroundColor Red
    Write-Host "Open deze map in Android Studio en kies: Build > Build APK(s)." -ForegroundColor Yellow
    exit 1
}

$apk = Join-Path $PSScriptRoot "app\build\outputs\apk\debug\app-debug.apk"

if (Test-Path $apk) {
    Write-Host ""
    Write-Host "APK aangemaakt:" -ForegroundColor Green
    Write-Host $apk
}
else {
    Write-Host "Build klaar, maar APK-pad niet gevonden." -ForegroundColor Yellow
}
