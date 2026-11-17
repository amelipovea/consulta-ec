@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\probar-fase3.ps1" %*
exit /b %errorlevel%
