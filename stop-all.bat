@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0corp-deposit-rag-server\scripts\stop-all.ps1" -ProjectRoot "%~dp0."
set "STOP_EXIT=%ERRORLEVEL%"
if not defined CORP_DEPOSIT_NO_PAUSE pause
exit /b %STOP_EXIT%
