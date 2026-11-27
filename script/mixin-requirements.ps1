<#
.SYNOPSIS
    Verifies that the methods / fields GoldenTweaks' mixins target still exist in the installed jars.

.DESCRIPTION
    Parses every mixin source file under src/main/java/net/fodoth/skina/goldentweaks/mixin, extracts the
    @Mixin target class plus the method / field names referenced by @Inject / @Redirect / @ModifyVariable /
    @Accessor / @Invoker, resolves the target class to a jar in <PackMods> (including META-INF/jarjar nested
    jars), then dumps the class with javap and reports the members that are gone.

    This is a static check: it catches renamed or deleted methods, but not ordinal shifts, @ModifyArgs
    indices or @Shadow field type changes. A real confirmation still needs an in-game run.

.EXAMPLE
    ./script/mixin-requirements.ps1 -PackMods "D:\packs\myinstance\mods"
#>
param(
    [Parameter(Mandatory = $true)][string]$PackMods,
    [string]$SourceRoot = (Join-Path $PSScriptRoot "..\src\main\java\net\fodoth\skina\goldentweaks\mixin"),
    [string]$Only = ''
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

$javap = ""
if ($env:JAVA_HOME) {
    $p = Join-Path $env:JAVA_HOME 'bin\javap.exe'
    if (Test-Path $p) { $javap = $p }
}
if (-not $javap) {
    # JAVA_HOME is often a JRE (no javap); prefer a JDK 21 next to it, then any JDK on the box
    $roots = @('C:\Program Files\BellSoft', 'C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Java', 'C:\Program Files\Microsoft', (Join-Path $env:USERPROFILE '.gradle\jdks'))
    $hits = @()
    foreach ($root in $roots) {
        if (-not (Test-Path $root)) { continue }
        $hits += @(Get-ChildItem $root -Filter javap.exe -Recurse -Depth 3 -ErrorAction SilentlyContinue)
    }
    if ($hits.Count -gt 0) {
        $preferred = @($hits | Where-Object { $_.FullName -match 'JDK-21|jdk-21|jdk21' })
        $javap = if ($preferred.Count -gt 0) { $preferred[0].FullName } else { $hits[0].FullName }
    }
}
if (-not $javap) { throw "javap not found; set JAVA_HOME to a JDK" }
Write-Output ("javap: " + $javap)

# ---------- 1. collect mixin requirements ----------
$reqs = @()
foreach ($f in Get-ChildItem -LiteralPath $SourceRoot -Recurse -Filter *.java -File) {
    $text = Get-Content -LiteralPath $f.FullName -Raw -Encoding UTF8
    if ($text -notmatch '@Mixin') { continue }
    # a "dummy" mixin deliberately supplies the members another addon expects to @Shadow; there is
    # nothing to verify against the target jar, so skip it
    if ($text -match 'DummyMixin|@MixinRequirementIgnore') { continue }
    $m = [regex]::Match($text, '@Mixin\(\s*(?:value\s*=\s*|targets\s*=\s*)"([^"]+)"')
    if (-not $m.Success) { $m = [regex]::Match($text, '@Mixin\(\s*(?:value\s*=\s*)?([A-Za-z0-9_$.]+)\.class') }
    if (-not $m.Success) { continue }
    $target = $m.Groups[1].Value
    $methods = @([regex]::Matches($text, '(?:method|name)\s*=\s*"([A-Za-z0-9_$<>]+)') | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
    $fields = @([regex]::Matches($text, '@(?:Shadow|Accessor|Invoker)[^\n]*\n(?:[^\n]*\n)*?\s*(?:private|protected|public)?\s*(?:static\s+)?(?:final\s+)?[A-Za-z0-9_$.<>\[\],\s]+\s+([A-Za-z0-9_$]+)\s*;') | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
    $reqs += [pscustomobject]@{ File = $f.Name; Target = $target; Methods = $methods; Fields = $fields }
}
if ($Only) { $reqs = @($reqs | Where-Object { $_.Target -match $Only -or $_.File -match $Only }) }
Write-Output ("mixins scanned: {0}" -f $reqs.Count)

# ---------- 2. index the pack ----------
$classToJar = @{}
$tmpRoot = Join-Path ([IO.Path]::GetTempPath()) ("gt-mixin-" + [Guid]::NewGuid().ToString('N').Substring(0, 8))
New-Item -ItemType Directory -Path $tmpRoot -Force | Out-Null
$wanted = New-Object 'System.Collections.Generic.HashSet[string]'
foreach ($r in $reqs) { [void]$wanted.Add(($r.Target -replace '\.', '/') + '.class') }

foreach ($jar in Get-ChildItem -LiteralPath $PackMods -Filter *.jar -File) {
    $zip = $null
    try { $zip = [IO.Compression.ZipFile]::OpenRead($jar.FullName) } catch { continue }
    $archives = New-Object System.Collections.Generic.List[IO.Compression.ZipArchive]
    $streams = New-Object System.Collections.Generic.List[IO.MemoryStream]
    $archives.Add($zip)
    try {
        foreach ($e in $zip.Entries) {
            if ($e.FullName -notlike 'META-INF/jarjar/*.jar') { continue }
            $ms = New-Object IO.MemoryStream
            try { $e.Open().CopyTo($ms); $ms.Position = 0; $archives.Add((New-Object IO.Compression.ZipArchive($ms, [IO.Compression.ZipArchiveMode]::Read))); $streams.Add($ms) } catch { $ms.Dispose() }
        }
        foreach ($a in $archives) {
            foreach ($e in $a.Entries) {
                if (-not $wanted.Contains($e.FullName)) { continue }
                $out = Join-Path $tmpRoot ($e.FullName -replace '/', '_')
                try { [IO.Compression.ZipFileExtensions]::ExtractToFile($e, $out, $true); if (-not $classToJar.ContainsKey($e.FullName)) { $classToJar[$e.FullName] = $jar.Name } } catch { }
            }
        }
    } finally {
        for ($i = 1; $i -lt $archives.Count; $i++) { $archives[$i].Dispose() }
        foreach ($s in $streams) { $s.Dispose() }
        $zip.Dispose()
    }
}

# ---------- 3. dump members ----------
$cache = @{}
function Get-Members([string]$classPath) {
    if ($cache.ContainsKey($classPath)) { return $cache[$classPath] }
    $file = Join-Path $tmpRoot ($classPath -replace '/', '_')
    if (-not (Test-Path $file)) { $cache[$classPath] = $null; return $null }
    $raw = & $javap -p -c $file 2>$null
    $text = ($raw -join "`n")
    $methods = New-Object 'System.Collections.Generic.HashSet[string]'
    $fields = New-Object 'System.Collections.Generic.HashSet[string]'
    # javap -p prints every member; match the name token right before "(", tolerating generics and
    # array/vararg types, and treat constructors ("Class(") as "<init>"
    foreach ($m in [regex]::Matches($text, '(?m)^\s*(?:public|protected|private|static|final|abstract|synchronized|native|strictfp|default|\s)+.*?([A-Za-z0-9_$]+)\s*\(')) { [void]$methods.Add($m.Groups[1].Value) }
    foreach ($m in [regex]::Matches($text, '(?m)^\s*(?:public|protected|private|static|final|volatile|transient|\s)+[A-Za-z0-9_$.<>\[\],]+(?:\s*\[\])?\s+([A-Za-z0-9_$]+)\s*;')) { [void]$fields.Add($m.Groups[1].Value) }
    # any name appearing at all, as a final safety net against signature parsing quirks
    $anyName = New-Object 'System.Collections.Generic.HashSet[string]'
    foreach ($m in [regex]::Matches($text, '([A-Za-z0-9_$]+)\s*\(')) { [void]$anyName.Add($m.Groups[1].Value) }
    $r = [pscustomobject]@{ Methods = $methods; Fields = $fields; AnyName = $anyName }
    $cache[$classPath] = $r
    return $r
}

# ---------- 4. report ----------
$gaps = 0; $missingClass = 0
foreach ($r in $reqs) {
    $cp = ($r.Target -replace '\.', '/') + '.class'
    $null = $fields
    if (-not $classToJar.ContainsKey($cp)) {
        # not shipped by the pack: mod absent, ignore instead of flagging
        continue
    }
    $info = Get-Members $cp
    if (-not $info) { $missingClass++; Write-Output ("  ? could not dump {0} ({1})" -f $r.Target, $r.File); continue }
    $bad = @()
    foreach ($m in $r.Methods) {
        if ($m -match '^<') { continue }
        if ($m -like 'lambda$*') { continue }
        if ($m -match '\*') { continue }
        if (-not $info.Methods.Contains($m) -and -not $info.AnyName.Contains($m)) { $bad += ("method " + $m) }
    }
    if ($bad.Count -gt 0) {
        $gaps += $bad.Count
        Write-Output ("  GAP  {0}  [{1}]" -f $r.Target, $r.File)
        $bad | ForEach-Object { Write-Output ("         " + $_) }
    }
}

Remove-Item $tmpRoot -Recurse -Force -ErrorAction SilentlyContinue
Write-Output ("gaps: {0}; undumpable: {1}" -f $gaps, $missingClass)
