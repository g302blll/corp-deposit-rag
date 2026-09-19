@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0corp-deposit-rag-server\scripts\start-all.ps1" -ProjectRoot "%~dp0."
set "START_EXIT=%ERRORLEVEL%"
if not "%START_EXIT%"=="0" echo. & echo Startup failed. Check logs in .runtime\logs.
if not defined CORP_DEPOSIT_NO_PAUSE pause
exit /b %START_EXIT%
