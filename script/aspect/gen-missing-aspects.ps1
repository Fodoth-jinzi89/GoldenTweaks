<#
.SYNOPSIS
    Fills the aspect gaps of a whole modpack: runs gen-aspects.ps1 for every namespace
    that still has item ids without an aspect definition.

.DESCRIPTION
    Scans all jars of a pack (including META-INF/jarjar nested jars), collects the item ids
    each namespace exposes (lang item./block. keys), subtracts the ids that already have a
    definition under <AspectRoot>\aspects and drives gen-aspects.ps1 for the rest.

    Namespaces listed in -Skip are ignored, which is meant for namespaces whose ids are not
    real items (minecraft vanilla assets are not shipped by pack jars, jaopca generates its
    ids at runtime, create_areofreight is a stale copy of create_aerofreight's assets).

    Dry run by default; pass -Apply to write the definitions.

.EXAMPLE
    ./script/aspect/gen-missing-aspects.ps1 -PackMods "D:\packs\myinstance\mods"

.EXAMPLE
    ./script/aspect/gen-missing-aspects.ps1 -PackMods "D:\packs\myinstance\mods" -Apply
#>
param(
    [Parameter(Mandatory = $true)][string]$PackMods,
    [string]$AspectRoot = (Join-Path $PSScriptRoot "..\..\src\main\resources\data\goldentweaks\recipe\thaumcraft"),
    [string[]]$Skip = @('minecraft', 'jaopca', 'create_areofreight', 'create_aerofreight', 'ccbr', 'cc_better_recipes', 'create_computercraft_integration', 'examplemod'),
    [string]$Only = '',
    [int]$MinItems = 1,
    [int]$Top = 0,
    [ValidateSet("items", "entities", "both")][string]$Kind = "items",
    [switch]$Apply
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

$gen = Join-Path $PSScriptRoot 'gen-aspects.ps1'

# --- ids already covered by an aspect definition ---
$covered = New-Object 'System.Collections.Generic.HashSet[string]'
foreach ($f in Get-ChildItem -LiteralPath $AspectRoot -Recurse -Filter *.json -File |
        Where-Object { $_.FullName -match '\\aspects\\|\\entity_aspects\\' }) {
    $m = [regex]::Match((Get-Content -LiteralPath $f.FullName -Raw -Encoding UTF8), '"id"\s*:\s*"([^"]+)"')
    if ($m.Success) { [void]$covered.Add($m.Groups[1].Value) }
}

# --- namespace -> ids and namespace -> providing jars ---
$nsIds = @{}
$nsJars = @{}
foreach ($jar in Get-ChildItem -LiteralPath $PackMods -Filter *.jar -File) {
    $zip = $null
    try { $zip = [IO.Compression.ZipFile]::OpenRead($jar.FullName) } catch { continue }
    $archives = New-Object System.Collections.Generic.List[IO.Compression.ZipArchive]
    $streams = New-Object System.Collections.Generic.List[IO.MemoryStream]
    $archives.Add($zip)
    try {
        # nested jars (META-INF/jarjar) ship namespaced assets too, the generator scans them as well
        foreach ($e in $zip.Entries) {
            if ($e.FullName -notlike 'META-INF/jarjar/*.jar') { continue }
            $ms = New-Object IO.MemoryStream
            try {
                $e.Open().CopyTo($ms)
                $ms.Position = 0
                $archives.Add((New-Object IO.Compression.ZipArchive($ms, [IO.Compression.ZipArchiveMode]::Read)))
                $streams.Add($ms)
            } catch { $ms.Dispose() }
        }
        # model / blockstate paths of this jar, per namespace (used to tell real ids from lang sub keys)
        $modelNs = @{}
        foreach ($archive in $archives) {
            foreach ($e in $archive.Entries) {
                if ($e.FullName -match '^assets/([^/]+)/(?:models/(?:item|block)|blockstates)/(.+)\.json$') {
                    $mns = $Matches[1]
                    if (-not $modelNs.ContainsKey($mns)) { $modelNs[$mns] = New-Object 'System.Collections.Generic.HashSet[string]' }
                    [void]$modelNs[$mns].Add($Matches[2])
                }
            }
        }
        foreach ($archive in $archives) {
            foreach ($e in $archive.Entries) {
                if ($e.FullName -notmatch '^assets/([^/]+)/lang/en_us\.json$') { continue }
                $ns = $Matches[1]
                if ($Skip -contains $ns) { continue }
                if ($Only -and $ns -notmatch $Only) { continue }
                if (-not $nsIds.ContainsKey($ns)) {
                    $nsIds[$ns] = New-Object 'System.Collections.Generic.HashSet[string]'
                    $nsJars[$ns] = New-Object System.Collections.Generic.List[string]
                }
                if (-not $nsJars[$ns].Contains($jar.FullName)) { [void]$nsJars[$ns].Add($jar.FullName) }
                $rd = New-Object IO.StreamReader($e.Open())
                $text = $rd.ReadToEnd()
                $rd.Close()
                $modelPaths = $null
                if ($modelNs.ContainsKey($ns)) { $modelPaths = $modelNs[$ns] }
                if ($Kind -eq 'items' -or $Kind -eq 'both') {
                    foreach ($m in [regex]::Matches($text, '"(item|block)\.([^"]+)"\s*:')) {
                        $rest = $m.Groups[2].Value
                        $dot = $rest.IndexOf('.')
                        $id = $null
                        if ($dot -lt 0) { $id = "$ns`:$rest" } else {
                            $head = $rest.Substring(0, $dot)
                            $tail = $rest.Substring($dot + 1)
                            if ($head -eq $ns) {
                                if ($tail -notmatch '\.') { $id = "$ns`:$tail" } else {
                                    $slashed = $tail -replace '\.', '/'
                                    if ($modelPaths -and $modelPaths.Contains($slashed)) { $id = "$ns`:$slashed" }
                                }
                            } elseif ($head -cmatch '^[a-z0-9_]+$' -and $tail -notmatch '[\.:]' -and $tail -notmatch '^(name|desc|description|tooltip|shift|hint|info|text|working|status|message|jei|jei_desc)$') {
                                $id = "$head`:$tail"
                            }
                        }
                        if ($id) { [void]$nsIds[$ns].Add($id) }
                    }
                }
                if ($Kind -eq 'entities' -or $Kind -eq 'both') {
                    foreach ($m in [regex]::Matches($text, '"entity\.([^"]+)"\s*:')) {
                        $rest = $m.Groups[1].Value
                        $dot = $rest.IndexOf('.')
                        if ($dot -lt 0) { $id = "$ns`:$rest" } else {
                            $head = $rest.Substring(0, $dot)
                            if ($head -eq $ns) { $id = "$ns`:$($rest.Substring($dot + 1))" } else { $id = "$ns`:$rest" }
                        }
                        if (($id -split ':', 2)[1] -notmatch '\.') { [void]$nsIds[$ns].Add($id) }
                    }
                }
            }
        }
    } finally {
        for ($i = 1; $i -lt $archives.Count; $i++) { $archives[$i].Dispose() }
        foreach ($s in $streams) { $s.Dispose() }
        $zip.Dispose()
    }
}

# --- queue the namespaces that still have uncovered ids ---
$queue = @()
foreach ($ns in $nsIds.Keys) {
    $uncovered = @($nsIds[$ns] | Where-Object { -not $covered.Contains($_) })
    if ($uncovered.Count -ge $MinItems) { $queue += [pscustomobject]@{ Ns = $ns; Uncovered = $uncovered.Count; Total = $nsIds[$ns].Count } }
}
$queue = $queue | Sort-Object Uncovered -Descending
if ($Top -gt 0) { $queue = $queue | Select-Object -First $Top }

# snapshot of the covered ids so every generator call does not rescan the aspect tree
$snapshot = Join-Path ([IO.Path]::GetTempPath()) "gt-covered-ids.txt"
[IO.File]::WriteAllLines($snapshot, $covered)

Write-Output ("namespaces to process: {0} (kind {1}, apply {2})" -f $queue.Count, $Kind, $Apply)
$written = 0
foreach ($row in $queue) {
    $sum = 0
    foreach ($jar in $nsJars[$row.Ns]) {
        $out = & $gen -Jar $jar -Namespace $row.Ns -Kind $Kind -AspectRoot $AspectRoot -CoveredPath $snapshot -Apply:$Apply 2>$null
        if ($Apply) {
            $m = [regex]::Match(($out -join "`n"), 'written: (\d+)')
            if ($m.Success) { $sum += [int]$m.Groups[1].Value }
        }
    }
    if ($Apply) {
        $written += $sum
        Write-Output ("  {0,5} uncovered /{1,5} total  {2}  -> written {3}" -f $row.Uncovered, $row.Total, $row.Ns, $sum)
    } else {
        Write-Output ("  {0,5} uncovered /{1,5} total  {2}  (jar: {3})" -f $row.Uncovered, $row.Total, $row.Ns, (Split-Path $nsJars[$row.Ns][0] -Leaf))
    }
}
if ($Apply) { Write-Output ("total written: {0}" -f $written) }
