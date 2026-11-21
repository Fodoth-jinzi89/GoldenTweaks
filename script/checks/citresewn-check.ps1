# Run after gradlew build. Supply the pack used for the original heap capture.
param(
    [string]$Instance = 'E:\机械动力魔法大冒险\.minecraft\versions\hkx',
    [string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21'
)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/citresewn-check'
New-Item -ItemType Directory -Force $output | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$cit = Get-ChildItem -LiteralPath (Join-Path $Instance 'mods') | Where-Object Name -EQ '[C]-citresewn-1.2.2-fabric-1.21.1.jar'
$zip = [IO.Compression.ZipFile]::OpenRead($cit.FullName)
try {
    $entry = $zip.GetEntry('META-INF/jars/citresewn-defaults-1.2.2+1.21.jar')
    $defaults = Join-Path $output 'defaults.jar'
    [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $defaults, $true)
} finally { $zip.Dispose() }
$libraries = Join-Path (Split-Path (Split-Path $Instance -Parent) -Parent) 'libraries/org/ow2/asm'
$jars = foreach ($module in @('asm', 'asm-tree', 'asm-analysis')) {
    Join-Path $libraries "$module/9.10.1/$module-9.10.1.jar"
}
$classpath = (@((Join-Path $root 'build/classes/java/main')) + $jars) -join ';'
& (Join-Path $Jdk 'bin/javac.exe') -encoding UTF-8 -cp $classpath -d $output (Join-Path $PSScriptRoot 'citresewn/Check.java')
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
& (Join-Path $Jdk 'bin/java.exe') -Xverify:all -cp "$output;$classpath" Check (Join-Path $Instance '.mixin.out/class/net/minecraft/world/item/ItemStack.class') $defaults
if ($LASTEXITCODE -ne 0) { throw 'CIT lazy-cache regression failed' }
