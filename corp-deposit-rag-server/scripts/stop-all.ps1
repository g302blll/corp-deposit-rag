param(
    [Parameter(Mandatory = $true)][string]$ProjectRoot,
    [switch]$Quiet
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath $ProjectRoot).Path.TrimEnd('\')
$pidFile = Join-Path $root '.runtime\pids.json'

if (-not (Test-Path -LiteralPath $pidFile)) {
    if (-not $Quiet) { Write-Host 'No tracked project processes are running.' -ForegroundColor Yellow }
    exit 0
}

$decodedRecords = Get-Content -LiteralPath $pidFile -Raw | ConvertFrom-Json
$records = @()
# Windows PowerShell 5 keeps a JSON array as one pipeline object; foreach reliably expands it.
foreach ($decodedRecord in $decodedRecords) { $records += $decodedRecord }
$remainingRecords = @()
foreach ($record in $records) {
    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $($record.pid)" -ErrorAction SilentlyContinue
    if ($null -eq $process) { continue }
    $normalizedCommand = $process.CommandLine.Replace('\\', '\')
    $normalizedMarker = ([string]$record.marker).Replace('\\', '\')
    if ([string]::IsNullOrWhiteSpace($normalizedMarker) -or
        $normalizedCommand.IndexOf($normalizedMarker, [StringComparison]::OrdinalIgnoreCase) -lt 0) {
        Write-Warning "Skipped PID $($record.pid): its command line does not match the recorded executable target."
        $remainingRecords += $record
        continue
    }
    try {
        Stop-Process -Id $record.pid -Force -ErrorAction Stop
        $deadline = (Get-Date).AddSeconds(5)
        while ($null -ne (Get-Process -Id $record.pid -ErrorAction SilentlyContinue) -and (Get-Date) -lt $deadline) {
            Start-Sleep -Milliseconds 100
        }
        if ($null -ne (Get-Process -Id $record.pid -ErrorAction SilentlyContinue)) {
            throw "PID $($record.pid) is still running."
        }
        if (-not $Quiet) { Write-Host "Stopped $($record.name) (PID $($record.pid))." }
    } catch {
        Write-Warning "Failed to stop $($record.name) (PID $($record.pid)): $($_.Exception.Message)"
        $remainingRecords += $record
    }
}

if ($remainingRecords.Count -gt 0) {
    $remainingRecords | ConvertTo-Json | Set-Content -LiteralPath $pidFile -Encoding UTF8
    throw 'Some tracked processes were not stopped because ownership verification failed.'
}
Remove-Item -LiteralPath $pidFile -Force
if (-not $Quiet) { Write-Host 'Project services stopped.' -ForegroundColor Green }
