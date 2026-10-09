<#
    transfer-parts.ps1
    Push every .7z / .jar file in THIS script's folder to a remote Windows host over SSH.
    Split volumes (name.7z.001, .002, ...) are picked up too and sent in order.

    Auth: if the SSH key below exists and is accepted, NO password is asked.
          Otherwise it falls back to a password prompt (once per file).
          Run setup-ssh-key.ps1 once to install the key and enable passwordless mode.

    Portable: uses only the built-in OpenSSH client and %USERPROFILE%\.ssh\goldentweaks_deploy_ed25519,
    so it runs the same on any Windows machine. If scp/ssh are missing, install the
    client (admin):
        Add-WindowsCapability -Online -Name OpenSSH.Client~~~~0.0.1.0

    Usage:
        powershell -ExecutionPolicy Bypass -File .\transfer-parts.ps1
        powershell -ExecutionPolicy Bypass -File .\transfer-parts.ps1 -SourceDir 'D:\backup' -DestDir 'C:/Users/Administrator/Desktop/'
        powershell -ExecutionPolicy Bypass -File .\transfer-parts.ps1 -DryRun

    Notes:
        - SourceDir defaults to the folder this script lives in (drop the files next to it).
        - Save as UTF-8 (with BOM) if you ever add non-ASCII characters.
        - Per-file transfer (no byte-level resume): a failed file is re-sent whole.
#>
[CmdletBinding()]
param(
    [string]$SourceDir    = $PSScriptRoot,
    [string]$DestDir      = 'C:/Users/Administrator/Desktop/',
    [string]$IdentityFile = (Join-Path $env:USERPROFILE '.ssh\goldentweaks_deploy_ed25519'),
    [switch]$DryRun
)

# ===================== CONFIG =====================
$Server  = '202.189.22.69'
$Port    = 20009
$User    = 'Administrator'
$Retries = 3
$Cipher  = 'aes128-gcm@openssh.com'   # much faster than the default cipher
# ==================================================

$ErrorActionPreference = 'Stop'
$Remote = '{0}@{1}' -f $User, $Server

function Get-Exe([string]$name) {
    $c = Get-Command $name -ErrorAction SilentlyContinue
    if (-not $c) {
        throw "'$name' not found. Install the OpenSSH Client (admin): Add-WindowsCapability -Online -Name OpenSSH.Client~~~~0.0.1.0"
    }
    $c.Source
}

$scp = Get-Exe scp
$ssh = Get-Exe ssh

# OpenSSH 9.0+ defaults scp to the SFTP protocol, which mishandles remote Windows
# drive paths unless -O (legacy protocol) is used. Detect and add -O only when required.
$ErrorActionPreference = 'SilentlyContinue'
$verText = (& $ssh -V 2>&1 | Out-String)
$ErrorActionPreference = 'Stop'
$useLegacy = $false
if ($verText -match 'OpenSSH[A-Za-z_]*_(\d+)\.') {
    if ([int]$Matches[1] -ge 9) { $useLegacy = $true }
}

# ---------- auth: prefer the key, fall back to password ----------
$useKey = $false
if ($IdentityFile -and (Test-Path -LiteralPath $IdentityFile)) {
    if ($DryRun) {
        $useKey = $true
        Write-Host ("Key    : {0}" -f $IdentityFile)
    } else {
        Write-Host ("Testing key auth: {0}" -f $IdentityFile)
        & $ssh -i $IdentityFile -p $Port -o BatchMode=yes -o StrictHostKeyChecking=accept-new -o ConnectTimeout=15 $Remote exit
        if ($LASTEXITCODE -eq 0) {
            $useKey = $true
            Write-Host '  key OK - no password will be requested'
        } else {
            Write-Warning '  key not accepted - falling back to password prompts (run setup-ssh-key.ps1)'
        }
    }
} else {
    Write-Host ("No SSH key at {0}" -f $IdentityFile)
    Write-Host '  -> password auth (prompt once per part). Run setup-ssh-key.ps1 for passwordless.'
}
Write-Host ''

# ---------- discover the files (.7z / .jar, incl. split volumes .7z.001, .002, ...) ----------
$src = (Resolve-Path -LiteralPath $SourceDir).Path
$files = Get-ChildItem -LiteralPath $src -File |
         Where-Object { $_.Extension -eq '.jar' -or $_.Name -match '(?i)\.7z(\.\d{3})?$' } |
         Sort-Object Name

if ($files.Count -lt 1) { throw "No .7z / .jar files found in $src" }

$sum = ($files | Measure-Object -Property Length -Sum).Sum
Write-Host ("Files  : {0}" -f $files.Count)
Write-Host ("Size   : {0:N2} GiB" -f ($sum / 1GB))
Write-Host ("Source : {0}" -f $src)
Write-Host ("Target : {0}:{1}" -f $Remote, $DestDir)
Write-Host ''

# ---------- transfer ----------
$n = 0
foreach ($f in $files) {
    $n++
    Write-Host ("[{0}/{1}] {2}  ({3:N2} MiB)" -f $n, $files.Count, $f.Name, ($f.Length / 1MB))

    $scpArgs = @('-P', $Port, '-c', $Cipher,
                 '-o', 'ServerAliveInterval=30',
                 '-o', 'ServerAliveCountMax=6',
                 '-o', 'StrictHostKeyChecking=accept-new')
    if ($useLegacy) { $scpArgs += '-O' }
    if ($useKey)    { $scpArgs += @('-i', $IdentityFile, '-o', 'BatchMode=yes') }
    $scpArgs += @($f.FullName, ('{0}:{1}' -f $Remote, $DestDir))

    if ($DryRun) {
        Write-Host ('  DRY: scp ' + ($scpArgs -join ' '))
        continue
    }

    $ok = $false
    for ($attempt = 1; $attempt -le $Retries; $attempt++) {
        & $scp @scpArgs
        if ($LASTEXITCODE -eq 0) { $ok = $true; break }
        Write-Warning ("  attempt {0}/{1} failed (exit {2})" -f $attempt, $Retries, $LASTEXITCODE)
        if ($attempt -lt $Retries) { Start-Sleep -Seconds 3 }
    }
    if (-not $ok) { throw "Giving up on '$($f.Name)' after $Retries attempts" }
    Write-Host '  ok'
}

Write-Host ''
if ($DryRun) {
    Write-Host 'Dry run complete - nothing was transferred.'
} else {
    Write-Host 'All files transferred.'
    Write-Host 'Verify the sizes on the server; for split archives extract the .001 with 7-Zip.'
}
