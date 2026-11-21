param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/emi-workstation-check'
New-Item -ItemType Directory -Force $output | Out-Null
@'
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fodoth.skina.goldentweaks.compat.emi.GTWorkstationRecipes;
public class Check {
    public static void main(String[] args) throws Exception {
        Object a=new String("same"), b=new String("same"), c=new Object();
        Map<String,List<Object>> map=new HashMap<>();
        map.put("first",new ArrayList<>(Arrays.asList(a,c,a,null)));
        map.put("second",new ArrayList<>(map.get("first")));
        map.put("equalButDistinct",new ArrayList<>(Arrays.asList(b,c,a,null)));
        map.put("reordered",new ArrayList<>(Arrays.asList(c,a,a,null)));
        map.put("empty",new ArrayList<>());
        GTWorkstationRecipes.compact(map);
        Field array=CopyOnWriteArrayList.class.getDeclaredField("array");array.setAccessible(true);
        List<Object> first=map.get("first"), second=map.get("second");
        if(first==second || array.get(first)!=array.get(second))throw new AssertionError("Array not shared independently");
        if(array.get(first)==array.get(map.get("equalButDistinct")))throw new AssertionError("Lost object identity");
        if(array.get(first)==array.get(map.get("reordered")))throw new AssertionError("Changed order");
        if(first.get(0)!=a || first.get(2)!=a || first.get(3)!=null)throw new AssertionError("Changed contents");
        first.set(0,c);first.add(b);first.remove(1);first.subList(0,1).clear();
        first.removeIf(Objects::isNull);first.replaceAll(v->a);first.sort((x,y)->0);
        if(!second.equals(Arrays.asList(a,c,a,null)))throw new AssertionError("Mutation leaked");
        second.clear();second.addAll(Arrays.asList(c,b));
        if(first.size()!=2 || first.get(0)!=a)throw new AssertionError("Reverse mutation leaked");
        GTWorkstationRecipes.compact(map);
        if(map.get("second").get(1)!=b)throw new AssertionError("Repeated compaction changed data");
        map.clear();map.put("reload",new ArrayList<>(List.of(c)));GTWorkstationRecipes.compact(map);
        if(map.size()!=1 || map.get("reload").getFirst()!=c)throw new AssertionError("Reload retained data");
        System.out.println("PASS: shared arrays; identity/order/duplicates/null preserved; independent mutation; repeated compaction and reload");
    }
}
'@ | Set-Content -LiteralPath (Join-Path $output 'Check.java') -Encoding utf8
$classpath = Join-Path $root 'build/classes/java/main'
& (Join-Path $Jdk 'bin/javac.exe') -encoding UTF-8 -cp $classpath -d $output (Join-Path $output 'Check.java')
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
& (Join-Path $Jdk 'bin/java.exe') --add-opens java.base/java.util.concurrent=ALL-UNNAMED -Xverify:all -cp "$output;$classpath" Check
if ($LASTEXITCODE -ne 0) { throw 'Workstation check failed' }
$emi = Join-Path $root 'libs/implementation/emi-1.1.24+1.21.1+neoforge.jar'
$bytecode = & (Join-Path $Jdk 'bin/javap.exe') -classpath $emi -c -p -s 'dev.emi.emi.registry.EmiRecipes$Manager'
if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect EMI manager' }
$text = $bytecode -join "`n"
if (-not $text.Contains('descriptor: (Ljava/util/List;Ljava/util/Map;Ljava/util/List;Z)V') -or
        -not $text.Contains('EmiRecipes.byWorkstation:Ljava/util/Map;') -or
        ([regex]::Matches($text, 'InterfaceMethod java/util/List.addAll:').Count -ne 1)) {
    throw 'Upstream EMI workstation construction changed'
}
Write-Output 'PASS: upstream constructor descriptor and workstation population site verified'
