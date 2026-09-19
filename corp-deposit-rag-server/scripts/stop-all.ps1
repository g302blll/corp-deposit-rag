param(
    [Parameter(Mandatory = $true)][string]$ProjectRoot,
    [switch]$Quiet
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath $ProjectRoot).Path.TrimEnd('\')
$pidFile = Join-Path $root '.runtime\pids.json'

# java.exe may be a Windows launcher that creates a second JVM. Discover every Java/Node
# process whose normalized command line contains this exact project root, including children.
$ownedProcesses = @(Get-CimInstance Win32_Process | Where-Object {
    $_.Name -in @('java.exe', 'node.exe') -and
    -not [string]::IsNullOrWhiteSpace($_.CommandLine) -and
    $_.CommandLine.Replace('\\', '\').IndexOf($root, [StringComparison]::OrdinalIgnoreCase) -ge 0
})

if ($ownedProcesses.Count -eq 0) {
    if (Test-Path -LiteralPath $pidFile) { Remove-Item -LiteralPath $pidFile -Force }
    if (-not $Quiet) { Write-Host 'No project processes are running.' -ForegroundColor Yellow }
    exit 0
}

foreach ($process in $ownedProcesses) {
    Stop-Process -Id $process.ProcessId -Force -ErrorAction SilentlyContinue
    if (-not $Quiet) { Write-Host "Stopped $($process.Name) (PID $($process.ProcessId))." }
}

if (Test-Path -LiteralPath $pidFile) { Remove-Item -LiteralPath $pidFile -Force }
if (-not $Quiet) { Write-Host 'Project services stopped.' -ForegroundColor Green }
