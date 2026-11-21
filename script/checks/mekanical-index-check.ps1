param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/mekanical-index-check'
New-Item -ItemType Directory -Force $output | Out-Null
@'
import java.util.*;
import net.fodoth.skina.goldentweaks.compat.mekanicalcreate.GTCandidateIndexes;
public class Check {
    public static void main(String[] args) {
        Object a=new String("same"), b=new String("same");
        Map<Object,List<Object>> index=new IdentityHashMap<>();
        Object first=new Object(), second=new Object(), distinct=new Object(), multiple=new Object(), empty=new Object();
        index.put(first,List.of(a));index.put(second,List.of(a));index.put(distinct,List.of(b));
        List<Object> pair=List.of(a,b);index.put(multiple,pair);index.put(empty,List.of());
        if(GTCandidateIndexes.compactSingletons(index)!=index)throw new AssertionError("Map identity changed");
        if(index.get(first)!=index.get(second))throw new AssertionError("Redundant singleton lists");
        if(index.get(first)==index.get(distinct) || index.get(distinct).getFirst()!=b)throw new AssertionError("Candidate identity changed");
        if(index.get(multiple)!=pair || !index.get(empty).isEmpty() || index.size()!=5)throw new AssertionError("Index changed");
        try {index.get(first).add(b);throw new AssertionError("Mutable singleton");} catch(UnsupportedOperationException expected) {}
        GTCandidateIndexes.compactSingletons(index);
        if(index.get(first)!=index.get(second))throw new AssertionError("Repeated compaction failed");
        Map<Object,List<Object>> reload=new IdentityHashMap<>();reload.put(first,List.of(b));GTCandidateIndexes.compactSingletons(reload);
        if(reload.get(first).getFirst()!=b || index.get(first).getFirst()!=a)throw new AssertionError("Reload leaked");
        System.out.println("PASS: singleton sharing; candidate identity, key identity, multi/empty lists, immutability and reload preserved");
    }
}
'@ | Set-Content (Join-Path $output 'Check.java') -Encoding utf8
$classpath = Join-Path $root 'build/classes/java/main'
& (Join-Path $Jdk 'bin/javac.exe') -encoding UTF-8 -cp $classpath -d $output (Join-Path $output 'Check.java')
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
& (Join-Path $Jdk 'bin/java.exe') -Xverify:all -cp "$output;$classpath" Check
if ($LASTEXITCODE -ne 0) { throw 'Index check failed' }
$jar = Get-ChildItem (Join-Path $root 'libs/compileOnly') -Filter '*mekanicalcreate*.jar' | Select-Object -First 1
$code = (& (Join-Path $Jdk 'bin/javap.exe') -c -p -classpath $jar.FullName 'io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver$CandidateCatalog') -join "`n"
if ($LASTEXITCODE -ne 0 -or ([regex]::Matches($code,'Method java/util/Collections.unmodifiableMap:').Count -ne 1) -or
        -not $code.Contains('InterfaceMethod java/util/List.copyOf:') -or -not $code.Contains('class java/util/IdentityHashMap')) {
    throw 'Upstream candidate index construction changed'
}
Write-Output 'PASS: upstream immutable lists and unique map wrapping site verified'
$metadata = (& (Join-Path $Jdk 'bin/javap.exe') -c -p -classpath $jar.FullName 'io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver$CandidateMetadata') -join "`n"
if ($LASTEXITCODE -ne 0 -or ([regex]::Matches($metadata, 'Method "\[I".clone:').Count -ne 2)) {
    throw 'Upstream metadata constructor/accessor defensive copies changed'
}
Write-Output 'PASS: upstream metadata constructor and accessor both defensively clone indexes'

Add-Type -AssemblyName System.IO.Compression.FileSystem
$artifact = Join-Path $root 'build/libs/goldentweaks-4.7.8.jar'
$zip = [System.IO.Compression.ZipFile]::OpenRead($artifact)
try {
    if ($zip.Entries.FullName -match '(GTRequirementIndexes|MekanicalCreateRequirementIndexesMixin)\.class$') {
        throw 'Broken requirement index optimization remains in artifact'
    }
    $reader = [System.IO.StreamReader]::new($zip.GetEntry('goldentweaks.mixins.json').Open())
    try { $config = $reader.ReadToEnd() } finally { $reader.Dispose() }
    if ($config.Contains('MekanicalCreateRequirementIndexesMixin')) { throw 'Broken mixin is still registered' }
} finally { $zip.Dispose() }
Write-Output 'PASS: built artifact contains no broken requirement-index mixin, helper or registration'
