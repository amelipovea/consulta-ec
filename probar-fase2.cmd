@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\probar-fase2.ps1"
exit /b %ERRORLEVEL%
