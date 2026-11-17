@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\probar-fase1.ps1"
exit /b %ERRORLEVEL%
