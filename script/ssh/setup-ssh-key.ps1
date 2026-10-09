<#
    setup-ssh-key.ps1
    One-time setup: install THIS machine's public key on a remote Windows host so
    later scp/ssh logins need NO password.

    The remote password is read from a file (default .\mima.txt).
    The first run needs internet (installs the Posh-SSH module from PSGallery, CurrentUser scope),
    because the built-in Windows ssh cannot accept a password non-interactively.

    Run it on the SAME machine that will do the file transfers.
    The window always stops at the end with "按回车键退出" (this file is UTF-8 with BOM).

    Usage:
        powershell -ExecutionPolicy Bypass -File .\setup-ssh-key.ps1
        powershell -ExecutionPolicy Bypass -File .\setup-ssh-key.ps1 -PasswordFile 'D:\secret\mima.txt'
        powershell -ExecutionPolicy Bypass -File .\setup-ssh-key.ps1 -DryRun

    After it succeeds, transfer-parts.ps1 uses the key automatically (it is the default
    identity). Deleting mima.txt afterwards is safe - it is no longer needed.
#>
[CmdletBinding()]
param(
    [string]$PasswordFile = '.\mima.txt',
    [string]$KeyPath      = (Join-Path $env:USERPROFILE '.ssh\goldentweaks_deploy_ed25519'),
    [switch]$DryRun
)

# ===================== CONFIG =====================
$Server = '202.189.22.69'
$Port   = 20009
$User   = 'Administrator'
# ==================================================

$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$script:exitCode = 0
try {
    # ---- read password ----
    if (-not (Test-Path -LiteralPath $PasswordFile)) { throw "Password file not found: $PasswordFile" }
    $Password = (Get-Content -LiteralPath $PasswordFile -Raw).Trim().TrimStart([char]0xFEFF)
    if ([string]::IsNullOrEmpty($Password)) { throw "Password file is empty: $PasswordFile" }
    Write-Host ("Password loaded ({0} chars) from {1}" -f $Password.Length, (Resolve-Path -LiteralPath $PasswordFile).Path)

    # ---- local keypair ----
    $sshDir = Split-Path -Parent $KeyPath
    if (-not (Test-Path $sshDir)) { New-Item -ItemType Directory -Path $sshDir -Force | Out-Null }
    if (-not (Test-Path -LiteralPath $KeyPath)) {
        Write-Host "Generating ed25519 key: $KeyPath"
        & ssh-keygen -t ed25519 -f $KeyPath -N '""' -q -C "$env:COMPUTERNAME-to-$Server"
        if ($LASTEXITCODE -ne 0 -or -not (Test-Path "$KeyPath.pub")) { throw 'ssh-keygen failed' }
    } else {
        Write-Host "Using existing key: $KeyPath"
    }
    $pubKey = (Get-Content -LiteralPath "$KeyPath.pub" -Raw).Trim()
    if ($pubKey -notmatch '^ssh-(ed25519|rsa) ') { throw "Unexpected public key content: $pubKey" }

    # ---- remote script: append our key to the Admin authorized_keys files ----
    # Windows OpenSSH uses %ProgramData%\ssh\administrators_authorized_keys for members of
    # the Administrators group (see the "Match Group administrators" block in sshd_config),
    # and ~/.ssh/authorized_keys otherwise. We write BOTH so either config works.
    $remoteTemplate = @'
$pub   = '__PUBKEY__'
$pubId = (($pub -split '\s+')[0..1] -join ' ')     # "ssh-ed25519 AAAA..." (key type + blob)
$adminFile = Join-Path $env:ProgramData 'ssh\administrators_authorized_keys'
$userFile  = Join-Path $env:USERPROFILE '.ssh\authorized_keys'
foreach ($f in @($adminFile, $userFile)) {
    $d = Split-Path -Parent $f
    if (-not (Test-Path $d)) { New-Item -ItemType Directory -Path $d -Force | Out-Null }
    $lines = @()
    if (Test-Path $f) {
        $lines = @(Get-Content -LiteralPath $f -ErrorAction SilentlyContinue) | Where-Object { $_ -and $_.Trim() }
    }
    $has = $false
    foreach ($l in $lines) { if ((($l -split '\s+')[0..1] -join ' ') -eq $pubId) { $has = $true } }
    if (-not $has) { $lines = @($lines) + $pub }
    Set-Content -LiteralPath $f -Value $lines -Encoding ascii      # ASCII, single key per line, no BOM
    $n  = @(Get-Content -LiteralPath $f -ErrorAction SilentlyContinue).Count
    $fp = (& ssh-keygen -lf $f 2>&1) -join '; '
    Write-Output ('written: ' + $f + '  keys=' + $n + '  fp=' + $fp)
}
icacls $adminFile /inheritance:r | Out-Null
icacls $adminFile /grant '*S-1-5-18:(F)' '*S-1-5-32-544:(F)' | Out-Null
icacls $adminFile /setowner '*S-1-5-32-544' | Out-Null
Write-Output 'acl: ok'
$svc = Get-Service sshd -ErrorAction SilentlyContinue
if ($svc) { Write-Output ('sshd: ' + $svc.Status) } else { Write-Output 'sshd: missing' }
'@

    $remote = $remoteTemplate.Replace('__PUBKEY__', $pubKey)
    $b64    = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($remote))
    $cmd    = "powershell -NoProfile -ExecutionPolicy Bypass -EncodedCommand $b64"

    if ($DryRun) {
        Write-Host ''
        Write-Host '--- DRY RUN (nothing will be sent) ---'
        Write-Host ("Target : {0}@{1}:{2}" -f $User, $Server, $Port)
        Write-Host 'Remote script:'
        Write-Host $remote
        return
    }

    # ---- ensure Posh-SSH is available ----
    if (-not (Get-Module -ListAvailable Posh-SSH)) {
        Write-Host 'Installing Posh-SSH (CurrentUser scope)...'
        if (-not (Get-PackageProvider -Name NuGet -ErrorAction SilentlyContinue)) {
            Install-PackageProvider -Name NuGet -MinimumVersion 2.8.5.201 -Force -Scope CurrentUser | Out-Null
        }
        Set-PSRepository -Name PSGallery -InstallationPolicy Trusted -ErrorAction SilentlyContinue
        Install-Module Posh-SSH -Scope CurrentUser -Force -AllowClobber -Confirm:$false
    }
    Import-Module Posh-SSH

    # ---- connect with the password and push the key ----
    $sec  = ConvertTo-SecureString $Password -AsPlainText -Force
    $cred = New-Object System.Management.Automation.PSCredential($User, $sec)
    Write-Host ("Connecting to {0}:{1} ..." -f $Server, $Port)
    $session = New-SSHSession -ComputerName $Server -Port $Port -Credential $cred -AcceptKey -ConnectionTimeout 20 -Force
    if (-not $session) { throw 'SSH connection failed (check host/port/credentials).' }
    try {
        $res = Invoke-SSHCommand -SessionId $session.SessionId -Command $cmd -TimeOut 60
        $res.Output | ForEach-Object { Write-Host ('  server: ' + $_) }
        if ($res.ExitStatus -ne 0) {
            $res.Error | ForEach-Object { Write-Host ('  server-err: ' + $_) }
            throw ('Remote command failed (exit {0})' -f $res.ExitStatus)
        }
    } finally {
        Remove-SSHSession -SessionId $session.SessionId | Out-Null
    }

    # ---- verify key-only login (BatchMode: fail instead of prompting) ----
    Write-Host 'Verifying key-based login (no password)...'
    & ssh -i $KeyPath -p $Port -o BatchMode=yes -o StrictHostKeyChecking=accept-new -o ConnectTimeout=15 "$User@$Server" exit
    if ($LASTEXITCODE -ne 0) { throw 'Key login verification failed (check sshd_config and the Administrators ACL on the server).' }

    Write-Host ''
    Write-Host 'SUCCESS - passwordless login works.'
    Write-Host "Key: $KeyPath"
    Write-Host 'transfer-parts.ps1 needs no change (this is the default identity).'
    Write-Host 'You can delete the password file now.'
}
catch {
    Write-Host ''
    Write-Host ('ERROR: ' + $_.Exception.Message) -ForegroundColor Red
    $script:exitCode = 1
}
finally {
    Write-Host ''
    try { Read-Host '按回车键退出' | Out-Null } catch { }
}

exit $script:exitCode
