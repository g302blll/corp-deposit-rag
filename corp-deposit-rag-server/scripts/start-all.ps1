param([Parameter(Mandatory = $true)][string]$ProjectRoot)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath $ProjectRoot).Path.TrimEnd('\')
$serverRoot = Join-Path $root 'corp-deposit-rag-server'
$webRoot = Join-Path $root 'corp-deposit-rag-web'
$runtimeRoot = Join-Path $root '.runtime'
$logRoot = Join-Path $runtimeRoot 'logs'
$pidFile = Join-Path $runtimeRoot 'pids.json'
$stopScript = Join-Path $serverRoot 'scripts\stop-all.ps1'

if (-not (Test-Path -LiteralPath (Join-Path $serverRoot 'pom.xml')) -or -not (Test-Path -LiteralPath (Join-Path $webRoot 'package.json'))) {
    throw 'Project layout is incomplete. Expected corp-deposit-rag-server and corp-deposit-rag-web.'
}

function Assert-Command([string]$Name) {
    if ($null -eq (Get-Command $Name -ErrorAction SilentlyContinue)) { throw "Required command not found: $Name" }
}

function Test-TcpPort([string]$HostName, [int]$Port, [int]$TimeoutMs = 1200) {
    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $task = $client.ConnectAsync($HostName, $Port)
        return $task.Wait($TimeoutMs) -and $client.Connected
    } catch { return $false } finally { $client.Dispose() }
}

function Wait-TcpPort([string]$Name, [int]$Port, [int]$TimeoutSeconds = 75) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-TcpPort '127.0.0.1' $Port 500) { Write-Host "  [OK] $Name :$Port" -ForegroundColor Green; return }
        Start-Sleep -Milliseconds 700
    }
    throw "$Name did not become ready on port $Port within $TimeoutSeconds seconds."
}

function Invoke-Checked([string]$Title, [scriptblock]$Action) {
    Write-Host "`n== $Title ==" -ForegroundColor Cyan
    & $Action
    if ($LASTEXITCODE -ne 0) { throw "$Title failed with exit code $LASTEXITCODE." }
}

Assert-Command 'java'
Assert-Command 'node'
Assert-Command 'npm.cmd'

foreach ($dependency in @(@{ Name = 'MySQL'; Port = 3306 }, @{ Name = 'Nacos'; Port = 8848 })) {
    if (-not (Test-TcpPort '127.0.0.1' $dependency.Port)) { throw "$($dependency.Name) is not reachable on 127.0.0.1:$($dependency.Port)." }
}
if (-not (Test-TcpPort '127.0.0.1' 6379)) { Write-Warning 'Redis is not reachable on port 6379. V1.2 main flow does not currently depend on it.' }

if ([string]::IsNullOrWhiteSpace($env:MYSQL_USERNAME)) { $env:MYSQL_USERNAME = 'root' }
if ([string]::IsNullOrWhiteSpace($env:MYSQL_PASSWORD)) {
    $securePassword = Read-Host 'Enter local MySQL password (not stored)' -AsSecureString
    $passwordPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try { $env:MYSQL_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPtr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPtr) }
}
if ([string]::IsNullOrWhiteSpace($env:NACOS_SERVER_ADDR)) { $env:NACOS_SERVER_ADDR = '127.0.0.1:8848' }

& $stopScript -ProjectRoot $root -Quiet
New-Item -ItemType Directory -Path $logRoot -Force | Out-Null

Push-Location $serverRoot
try { Invoke-Checked 'Build and test backend JARs' { & (Join-Path $serverRoot 'mvnw.cmd') clean package } }
finally { Pop-Location }

Push-Location $webRoot
try {
    Invoke-Checked 'Install frontend dependencies' { & npm.cmd ci }
    Invoke-Checked 'Run frontend tests' { & npm.cmd test }
    Invoke-Checked 'Build frontend' { & npm.cmd run build }
} finally { Pop-Location }

$services = @(
    @{ Name = 'customer-service'; Port = 8081 },
    @{ Name = 'deposit-product-service'; Port = 8082 },
    @{ Name = 'deposit-business-service'; Port = 8083 },
    @{ Name = 'ai-assistant-service'; Port = 8084 }
)
$processRecords = [System.Collections.Generic.List[object]]::new()
try {
    Write-Host "`n== Start services ==" -ForegroundColor Cyan
    foreach ($service in $services) {
        $jar = Get-ChildItem -LiteralPath (Join-Path $serverRoot "$($service.Name)\target") -Filter '*.jar' |
            Where-Object { $_.Name -notlike '*.original' } | Select-Object -First 1
        if ($null -eq $jar) { throw "JAR not found for $($service.Name)." }
        $stdout = Join-Path $logRoot "$($service.Name).out.log"
        $stderr = Join-Path $logRoot "$($service.Name).err.log"
        $process = Start-Process -FilePath 'java.exe' -ArgumentList @('-jar', $jar.FullName) -WorkingDirectory $serverRoot -RedirectStandardOutput $stdout -RedirectStandardError $stderr -WindowStyle Hidden -PassThru
        $processRecords.Add([pscustomobject]@{ name = $service.Name; pid = $process.Id })
    }

    $viteScript = Join-Path $webRoot 'node_modules\vite\bin\vite.js'
    $webProcess = Start-Process -FilePath 'node.exe' -ArgumentList @($viteScript, '--host', '127.0.0.1') -WorkingDirectory $webRoot -RedirectStandardOutput (Join-Path $logRoot 'web.out.log') -RedirectStandardError (Join-Path $logRoot 'web.err.log') -WindowStyle Hidden -PassThru
    $processRecords.Add([pscustomobject]@{ name = 'corp-deposit-rag-web'; pid = $webProcess.Id })
    $processRecords | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $runtimeRoot 'pids.json') -Encoding UTF8

    foreach ($service in $services) { Wait-TcpPort $service.Name $service.Port }
    Wait-TcpPort 'corp-deposit-rag-web' 5173
    # Keep this payload ASCII-only for compatibility with Windows PowerShell 5 body encoding.
    $smokeJson = '{"customerNo":"CUST001","message":"8000000"}'
    $response = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:5173/api/v1/assistant/plans' -ContentType 'application/json' -Body $smokeJson
    if ($null -eq $response.plans) { throw 'End-to-end recommendation smoke test returned no plans field.' }
} catch {
    if (Test-Path -LiteralPath (Join-Path $runtimeRoot 'pids.json')) { & $stopScript -ProjectRoot $root -Quiet }
    throw
}

Write-Host "`nAll services are ready: http://127.0.0.1:5173" -ForegroundColor Green
Write-Host "Logs: $logRoot"
if ($env:CORP_DEPOSIT_NO_BROWSER -ne '1') { Start-Process 'http://127.0.0.1:5173' }
