<#
.SYNOPSIS
    Checks GoldenTweaks' Thaumcraft aspect definitions against the mods installed in a pack.

.DESCRIPTION
    Reads every item/entity aspect definition under
    src/main/resources/data/goldentweaks/recipe/thaumcraft/{aspects,entity_aspects},
    collects the item ids the given mods folder actually provides (lang item./block. keys,
    models/item, models/block, blockstates, including META-INF/jarjar nested jars) and reports:

      * aspect entries that reference an id the pack no longer provides
      * item namespaces that have no aspect entry at all (mods with zero coverage)

.EXAMPLE
    ./script/aspect/check-aspect-coverage.ps1 -PackMods "D:\packs\myinstance\mods"

.EXAMPLE
    ./script/aspect/check-aspect-coverage.ps1 -PackMods "D:\packs\myinstance\mods" `
        -OutMissing missing-ids.txt -OutMods mod-coverage.txt
#>
param(
    [Parameter(Mandatory = $true)][string]$PackMods,
    [string]$AspectRoot = (Join-Path $PSScriptRoot "..\..\src\main\resources\data\goldentweaks\recipe\thaumcraft"),
    [string]$OutMissing = "",
    [string]$OutMods = "",
    [int]$TopMods = 40
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Get-ArchiveSet([string]$jarPath) {
    $zip = [IO.Compression.ZipFile]::OpenRead($jarPath)
    $archives = New-Object System.Collections.Generic.List[IO.Compression.ZipArchive]
    $streams = New-Object System.Collections.Generic.List[IO.MemoryStream]
    $archives.Add($zip)
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
    return [pscustomobject]@{ Outer = $zip; Archives = $archives; Streams = $streams }
}

function Close-ArchiveSet($set) {
    for ($i = 1; $i -lt $set.Archives.Count; $i++) { $set.Archives[$i].Dispose() }
    foreach ($s in $set.Streams) { $s.Dispose() }
    $set.Outer.Dispose()
}

$valid = New-Object 'System.Collections.Generic.HashSet[string]'
$packItems = New-Object 'System.Collections.Generic.HashSet[string]'
$nested = 0

foreach ($jar in Get-ChildItem -LiteralPath $PackMods -Filter *.jar -File) {
    try { $set = Get-ArchiveSet $jar.FullName } catch { continue }
    try {
        $nested += $set.Archives.Count - 1
        foreach ($archive in $set.Archives) {
            foreach ($e in $archive.Entries) {
                $n = $e.FullName
                if ($n -match '^assets/([^/]+)/(?:models/(?:item|block)|blockstates)/(.+)\.json$') {
                    [void]$valid.Add($Matches[1] + ':' + $Matches[2])
                }
                if ($n -notmatch '^assets/([^/]+)/lang/en_us\.json$') { continue }
                $ns = $Matches[1]
                $reader = New-Object IO.StreamReader($e.Open())
                $text = $reader.ReadToEnd()
                $reader.Close()
                foreach ($m in [regex]::Matches($text, '"(item|block|entity)\.([^"]+)"\s*:')) {
                    $rest = $m.Groups[2].Value
                    $dot = $rest.IndexOf('.')
                    if ($dot -lt 0) {
                        $id = "$ns`:$rest"
                    } else {
                        $head = $rest.Substring(0, $dot)
                        $tail = $rest.Substring($dot + 1)
                        if ($head -eq $ns) { $id = "$ns`:$tail" } else { $id = "$ns`:$rest" }
                    }
                    [void]$valid.Add($id)
                    if ($m.Groups[1].Value -ne 'entity') { [void]$packItems.Add($id) }
                }
            }
        }
    } finally { Close-ArchiveSet $set }
}

# --- aspect definitions on disk ---
$files = Get-ChildItem -LiteralPath $AspectRoot -Recurse -Filter *.json -File |
    Where-Object { $_.FullName -match '\\aspects\\|\\entity_aspects\\' }
$covered = New-Object 'System.Collections.Generic.HashSet[string]'
$missing = New-Object System.Collections.Generic.List[object]
foreach ($f in $files) {
    $text = Get-Content -LiteralPath $f.FullName -Raw -Encoding UTF8
    $m = [regex]::Match($text, '"id"\s*:\s*"([^"]+)"')
    if (-not $m.Success) { continue }
    $id = $m.Groups[1].Value
    [void]$covered.Add($id)
    if (-not $valid.Contains($id)) {
        $missing.Add([pscustomobject]@{ Id = $id; File = $f.FullName.Substring($AspectRoot.Length + 1) })
    }
}

Write-Output ("jars: {0} (+{1} jarJar); known ids: {2}; aspect entries: {3}" -f
    (Get-ChildItem -LiteralPath $PackMods -Filter *.jar -File).Count, $nested, $valid.Count, $covered.Count)
Write-Output ("entries referencing unknown ids: {0}" -f $missing.Count)
$missing | Group-Object { ($_.Id -split ':')[0] } | Sort-Object Count -Descending |
    ForEach-Object { Write-Output ("  {0,6}  {1}" -f $_.Count, $_.Name) }

# --- per namespace coverage ---
$byNs = @{}
foreach ($id in $packItems) {
    $ns = ($id -split ':')[0]
    if (-not $byNs.ContainsKey($ns)) { $byNs[$ns] = [pscustomobject]@{ Total = 0; Covered = 0 } }
    $byNs[$ns].Total++
    if ($covered.Contains($id)) { $byNs[$ns].Covered++ }
}

$uncoveredMods = @()
foreach ($ns in $byNs.Keys) {
    if ($byNs[$ns].Covered -eq 0) {
        $uncoveredMods += [pscustomobject]@{ Namespace = $ns; Items = $byNs[$ns].Total }
    }
}
Write-Output ("namespaces with items but no aspect data: {0}" -f $uncoveredMods.Count)
$uncoveredMods | Sort-Object Items -Descending |
    ForEach-Object { Write-Output ("  {0,6} items  {1}" -f $_.Items, $_.Namespace) }

Write-Output ("partial coverage (lowest first, top {0}):" -f $TopMods)
$byNs.GetEnumerator() |
    Where-Object { $_.Value.Covered -gt 0 -and $_.Value.Covered -lt $_.Value.Total } |
    Sort-Object { $_.Value.Covered / $_.Value.Total } |
    Select-Object -First $TopMods |
    ForEach-Object { Write-Output ("  {0,4}/{1,-4} {2}" -f $_.Value.Covered, $_.Value.Total, $_.Key) }

if ($OutMissing) {
    $missing | ForEach-Object { "{0}`t{1}" -f $_.Id, $_.File } | Set-Content -LiteralPath $OutMissing -Encoding UTF8
    Write-Output ("missing-id detail -> {0}" -f $OutMissing)
}
if ($OutMods) {
    $byNs.GetEnumerator() | Sort-Object { $_.Value.Covered / [Math]::Max(1, $_.Value.Total) } |
        ForEach-Object { "{0}`t{1}`t{2}" -f $_.Key, $_.Value.Covered, $_.Value.Total } |
        Set-Content -LiteralPath $OutMods -Encoding UTF8
    Write-Output ("namespace coverage -> {0}" -f $OutMods)
}
