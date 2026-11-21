<#
.SYNOPSIS
    Checks (and optionally regenerates) the per-mixin mod requirements used by
    GoldenTweaksMixinPlugin#shouldApplyMixin.

.DESCRIPTION
    A mixin class that links a class from an absent mod cannot be resolved by the mixin
    transformer, and the resulting NoClassDefFoundError is rethrown as a MixinTransformerError,
    aborting startup. This script walks every mixin listed in goldentweaks.mixins.json, maps the
    imports of its own class to the mod that provides them, and compares that with the guards in
    GoldenTweaksMixinPlugin.

    Without -Write it only reports the mods that are neither covered by the plugin's if-chain nor
    listed in MixinRequirements.java (expected: none). With -Write it rewrites
    MixinRequirements.java so the checker can enforce them.

.EXAMPLE
    ./script/mixin-requirements.ps1
    ./script/mixin-requirements.ps1 -Write
#>
param(
    [string]$LibsDir = (Join-Path $PSScriptRoot "..\libs"),
    [string]$SrcRoot = (Join-Path $PSScriptRoot "..\src\main\java"),
    [string]$JsonFile = (Join-Path $PSScriptRoot "..\src\main\resources\goldentweaks.mixins.json"),
    [string]$PluginFile = (Join-Path $PSScriptRoot "..\src\main\java\net\fodoth\skina\goldentweaks\mixin\GoldenTweaksMixinPlugin.java"),
    [string]$OutFile = (Join-Path $PSScriptRoot "..\src\main\java\net\fodoth\skina\goldentweaks\mixin\MixinRequirements.java"),
    [switch]$Write
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Get-PrimaryModId([string]$Path) {
    try { $zip = [System.IO.Compression.ZipFile]::OpenRead($Path) } catch { return $null }
    try {
        foreach ($name in @("META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
            $entry = $zip.Entries | Where-Object { $_.FullName -eq $name } | Select-Object -First 1
            if (-not $entry) { continue }
            $reader = New-Object System.IO.StreamReader($entry.Open())
            $text = $reader.ReadToEnd()
            $reader.Close()
            $blocks = [regex]::Split($text, '(?m)^\s*\[\[mods\]\]')
            if ($blocks.Count -lt 2) { continue }
            $id = [regex]::Match($blocks[1], '(?m)^\s*modId\s*=\s*"([^"]+)"').Groups[1].Value
            if ($id) { return $id }
        }
    } finally { $zip.Dispose() }
    return $null
}

# class -> providing jar ; jar -> primary mod id
$classJar = @{}
$jarMod = @{}
foreach ($sub in @("compileOnly", "implementation")) {
    $dir = Join-Path $LibsDir $sub
    if (-not (Test-Path $dir)) { continue }
    foreach ($file in Get-ChildItem $dir -Filter *.jar) {
        $jarMod[$file.Name] = Get-PrimaryModId $file.FullName
        $zip = [System.IO.Compression.ZipFile]::OpenRead($file.FullName)
        try {
            foreach ($entry in $zip.Entries) {
                if ($entry.FullName -like "*.class") {
                    $name = $entry.FullName.Substring(0, $entry.FullName.Length - 6)
                    if (-not $classJar.ContainsKey($name)) { $classJar[$name] = $file.Name }
                }
            }
        } finally { $zip.Dispose() }
    }
}

function Find-ProvidingJar([string]$internalName) {
    if ($classJar.ContainsKey($internalName)) { return $classJar[$internalName] }
    $parts = $internalName -split '/'
    for ($i = $parts.Count - 1; $i -ge 2; $i--) {
        $probe = ($parts[0..($i - 1)] -join '/')
        if ($classJar.ContainsKey($probe)) { return $classJar[$probe] }
    }
    return $null
}

# ---- guards already written into shouldApplyMixin
$plugin = Get-Content -LiteralPath $PluginFile -Raw -Encoding UTF8
$methodStart = [regex]::Match($plugin, 'public boolean shouldApplyMixin\s*\([^)]*\)\s*\{')
$bodyStart = $methodStart.Index + $methodStart.Length
$depth = 1
$cursor = $bodyStart
while ($cursor -lt $plugin.Length -and $depth -gt 0) {
    $ch = $plugin[$cursor]
    if ($ch -eq '{') { $depth++ } elseif ($ch -eq '}') { $depth-- }
    $cursor++
}
$methodBody = $plugin.Substring($bodyStart, ($cursor - 1) - $bodyStart)

$guards = @()
$offset = 0
while ($true) {
    $hit = [regex]::Match($methodBody.Substring($offset), '(?m)^\s*if\s*\(')
    if (-not $hit.Success) { break }
    $parenOpen = $offset + $hit.Index + $hit.Length - 1
    $level = 0
    $p = $parenOpen
    while ($p -lt $methodBody.Length) {
        if ($methodBody[$p] -eq '(') { $level++ } elseif ($methodBody[$p] -eq ')') { $level--; if ($level -eq 0) { break } }
        $p++
    }
    $condition = $methodBody.Substring($parenOpen + 1, $p - $parenOpen - 1)
    $q = $p + 1
    while ($q -lt $methodBody.Length -and [char]::IsWhiteSpace($methodBody[$q])) { $q++ }
    if ($q -lt $methodBody.Length -and $methodBody[$q] -eq '{') {
        $level = 0
        $b = $q
        while ($b -lt $methodBody.Length) {
            if ($methodBody[$b] -eq '{') { $level++ } elseif ($methodBody[$b] -eq '}') { $level--; if ($level -eq 0) { break } }
            $b++
        }
        $body = $methodBody.Substring($q + 1, $b - $q - 1)
        $next = $b + 1
    } else {
        $semi = $methodBody.IndexOf(';', $q)
        if ($semi -lt 0) { break }
        $body = $methodBody.Substring($q, $semi - $q + 1)
        $next = $semi + 1
    }
    $guards += [pscustomobject]@{ Condition = $condition; Text = "$condition`n$body" }
    $offset = $next
}

function Get-Guard([string]$fqn) {
    foreach ($guard in $guards) {
        foreach ($hit in [regex]::Matches($guard.Condition, '(startsWith|endsWith|contains)\s*\(\s*"([^"]+)"\s*\)')) {
            $kind = $hit.Groups[1].Value
            $pattern = $hit.Groups[2].Value
            if (($kind -eq 'startsWith' -and $fqn.StartsWith($pattern)) -or
                ($kind -eq 'endsWith' -and $fqn.EndsWith($pattern)) -or
                ($kind -eq 'contains' -and $fqn.Contains($pattern))) {
                return $guard
            }
        }
    }
    return $null
}

# ---- requirements already listed in MixinRequirements.java
$declared = @{}
if (Test-Path -LiteralPath $OutFile) {
    $existing = Get-Content -LiteralPath $OutFile -Raw -Encoding UTF8
    foreach ($entry in [regex]::Matches($existing, 'Map\.entry\("([^"]+)",\s*new String\[\]\s*\{([^}]*)\}\)')) {
        $mods = [regex]::Matches($entry.Groups[2].Value, '"([^"]+)"') | ForEach-Object { $_.Groups[1].Value }
        $declared[$entry.Groups[1].Value] = @($mods)
    }
}

# ---- walk the mixin config
$config = Get-Content -LiteralPath $JsonFile -Raw -Encoding UTF8 | ConvertFrom-Json
$mixinNames = @()
foreach ($section in @("mixins", "client")) { foreach ($name in $config.$section) { $mixinNames += $name } }

$ignored = '^(java|javax|net\.minecraft|net\.neoforged|com\.mojang|org\.spongepowered|com\.llamalad7|net\.fodoth)\.'
$requirements = @{}
$gaps = @()

foreach ($relative in $mixinNames) {
    $fqn = "net.fodoth.skina.goldentweaks.mixin.$relative"
    $file = Join-Path $SrcRoot (($fqn -replace '\.', '\') + ".java")
    if (-not (Test-Path -LiteralPath $file)) { continue }
    $source = Get-Content -LiteralPath $file -Raw -Encoding UTF8

    $needed = @{}
    foreach ($hit in [regex]::Matches($source, '(?m)^\s*import\s+(?:static\s+)?([\w\.\$]+)\s*;')) {
        $import = $hit.Groups[1].Value
        if ($import -match $ignored) { continue }
        $jar = Find-ProvidingJar ($import -replace '\.', '/')
        if (-not $jar) { continue }
        $modId = $jarMod[$jar]
        if (-not $modId) { continue }
        if ($modId -match '[\$\{\}]') { continue }
        $needed[$modId] = $true
    }
    if ($needed.Count -eq 0) { continue }

    $guard = Get-Guard $fqn
    $uncovered = @()
    foreach ($modId in $needed.Keys) {
        $covered = $false
        if ($guard) {
            $covered = $guard.Text -match ('isModLoaded\("' + [regex]::Escape($modId) + '"\)')
            if (-not $covered) {
                foreach ($hit in [regex]::Matches($guard.Text, 'checkIfPresent\("([^"]+)"\)')) {
                    $jar = Find-ProvidingJar ($hit.Groups[1].Value -replace '\.', '/')
                    if ($jar -and $jarMod[$jar] -eq $modId) { $covered = $true; break }
                }
            }
        }
        if (-not $covered) { $uncovered += $modId }
    }
    if ($uncovered.Count -eq 0) { continue }

    $uncovered = @($uncovered | Sort-Object)
    $requirements[$fqn] = $uncovered

    $already = @($declared[$fqn])
    $stillMissing = @($uncovered | Where-Object { $already -notcontains $_ })
    if ($stillMissing.Count -gt 0) {
        $gaps += [pscustomobject]@{ Mixin = $fqn; Mods = ($stillMissing -join ', ') }
    }
}

Write-Output "mixins checked: $($mixinNames.Count); mixins needing a requirements entry: $($requirements.Count)"
Write-Output "gaps not covered by MixinRequirements.java: $($gaps.Count)"
$gaps | ForEach-Object { Write-Output ("  {0}  ->  {1}" -f $_.Mixin, $_.Mods) }

if (-not $Write) {
    if ($gaps.Count -eq 0) { Write-Output "OK: every mixin is guarded." }
    return
}

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("package net.fodoth.skina.goldentweaks.mixin;")
$lines.Add("")
$lines.Add("import java.util.Map;")
$lines.Add("")
$lines.Add("/**")
$lines.Add(" * Generated by {@code script/mixin-requirements.ps1}; do not edit by hand.")
$lines.Add(" *")
$lines.Add(" * <p>Maps a mixin to the mods whose classes that mixin class itself links against. A mixin that")
$lines.Add(" * references a class from an absent mod cannot be resolved by the mixin transformer, and the")
$lines.Add(" * resulting {@code NoClassDefFoundError} is rethrown as a {@code MixinTransformerError} that aborts")
$lines.Add(" * startup, so {@link GoldenTweaksMixinPlugin#shouldApplyMixin} skips it up front.</p>")
$lines.Add(" */")
$lines.Add("final class MixinRequirements {")
$lines.Add("")
$lines.Add("    private MixinRequirements() {")
$lines.Add("    }")
$lines.Add("")
$lines.Add("    static final Map<String, String[]> BY_MIXIN = Map.ofEntries(")
$keys = $requirements.Keys | Sort-Object
for ($i = 0; $i -lt $keys.Count; $i++) {
    $mods = ($requirements[$keys[$i]] | ForEach-Object { '"' + $_ + '"' }) -join ', '
    $separator = if ($i -lt $keys.Count - 1) { "," } else { "" }
    $lines.Add("            Map.entry(""$($keys[$i])"", new String[] { $mods })$separator")
}
$lines.Add("    );")
$lines.Add("}")
[System.IO.File]::WriteAllLines($OutFile, $lines, (New-Object System.Text.UTF8Encoding($false)))
Write-Output "wrote $OutFile"
