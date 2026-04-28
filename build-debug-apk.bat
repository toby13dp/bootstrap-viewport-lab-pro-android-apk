@echo off
title Build Bootstrap Viewport Lab Pro APK
cd /d "%~dp0"
echo.
echo Bootstrap Viewport Lab Pro APK build
echo Package: be.creatieplezier.viewportlab
echo Localhost: http://127.0.0.1:53847
echo.

if exist gradlew.bat (
  call gradlew.bat assembleDebug
) else (
  gradle assembleDebug
)

echo.
if exist app\build\outputs\apk\debug\app-debug.apk (
  echo APK aangemaakt:
  echo app\build\outputs\apk\debug\app-debug.apk
) else (
  echo APK niet gevonden. Open deze map in Android Studio en kies Build APK(s).
)
pause
