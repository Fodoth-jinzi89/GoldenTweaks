param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/emi-snapshot-check'
New-Item -ItemType Directory -Force $output | Out-Null
@'
import java.util.*;
import net.fodoth.skina.goldentweaks.compat.emi.GTIngredientSnapshots;
public class Check {
    public static void main(String[] args) {
        Object a=new String("same"),b=new String("same"),c=new Object();
        List<Object> original=List.of(a,c,a), copy=List.copyOf(new ArrayList<>(original));
        if(GTIngredientSnapshots.reuse(original,copy)!=original)throw new AssertionError("Snapshot not shared");
        for(List<Object> changed:List.of(List.of(b,c,a),List.of(c,a,a),List.of(a,c),List.of(a,c,a,c))) {
            if(GTIngredientSnapshots.reuse(original,changed)!=changed)throw new AssertionError("Changed snapshot reused");
        }
        if(original.get(0)!=a || original.get(2)!=a)throw new AssertionError("Duplicates changed");
        try {GTIngredientSnapshots.reuse(original,copy).clear();throw new AssertionError("Mutable snapshot");}catch(UnsupportedOperationException expected){}
        System.out.println("PASS: immutable snapshot reuse; identity/order/duplicates/size and changed-input fallback preserved");
    }
}
'@ | Set-Content (Join-Path $output 'Check.java') -Encoding utf8
$cp = Join-Path $root 'build/classes/java/main'
& (Join-Path $Jdk 'bin/javac.exe') -proc:none -encoding UTF-8 -cp $cp -d $output (Join-Path $output 'Check.java')
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
& (Join-Path $Jdk 'bin/java.exe') -Xverify:all -cp "$output;$cp" Check
if ($LASTEXITCODE -ne 0) { throw 'Snapshot reuse check failed' }
$emi = Join-Path $root 'libs/implementation/emi-1.1.24+1.21.1+neoforge.jar'
$code = (& (Join-Path $Jdk 'bin/javap.exe') -p -c -classpath $emi dev.emi.emi.api.stack.ListEmiIngredient) -join "`n"
if ($LASTEXITCODE -ne 0 -or -not $code.Contains('InterfaceMethod java/util/stream/Stream.toList:') -or
        -not $code.Contains('public dev.emi.emi.api.stack.EmiIngredient copy();') -or
        -not $code.Contains('InterfaceMethod dev/emi/emi/api/stack/EmiIngredient.setChance:') -or
        ([regex]::Matches($code,'putfield\s+#\d+\s+// Field fullList:').Count -ne 1)) {
    throw 'Upstream snapshot immutability/copy implementation changed'
}
Write-Output 'PASS: upstream immutable snapshot construction and original copy/chance behavior verified'
