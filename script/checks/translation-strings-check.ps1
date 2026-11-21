param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/translation-strings-check'
New-Item -ItemType Directory -Force $output | Out-Null
@'
import java.util.*;
import net.fodoth.skina.goldentweaks.util.GTTranslationStrings;
public class Check {
 public static void main(String[] args) {
  Map<String,String> input=new LinkedHashMap<>();
  input.put(new String("a"),new String("你好 %s %% §a"));
  input.put(new String("b"),new String("你好 %s %% §a"));
  input.put(new String("你好 %s %% §a"),new String("different"));
  for(int i=0;i<100000;i++)input.put("test."+i,new String("repeated"));
  var result=GTTranslationStrings.compact(input);
  if(!result.equals(input)||!new ArrayList<>(result.keySet()).equals(new ArrayList<>(input.keySet())))throw new AssertionError("Content/order changed");
  if(result.get("a")!=result.get("b")||result.get("test.0")!=result.get("test.99999"))throw new AssertionError("Duplicate text not shared");
  String key=result.keySet().stream().filter(s->s.equals("你好 %s %% §a")).findFirst().orElseThrow();
  if(key!=result.get("a"))throw new AssertionError("Key/value text not shared");
  if(input.get("a")==input.get("b"))throw new AssertionError("Input changed");
  result.put("a","changed");if(!input.get("a").equals("你好 %s %% §a"))throw new AssertionError("Input mutation");
  var reload=GTTranslationStrings.compact(Map.of("a","new"));if(!reload.get("a").equals("new"))throw new AssertionError("Reload stale");
  if(!GTTranslationStrings.compact(Map.of()).isEmpty())throw new AssertionError("Empty input");
  System.out.println("PASS: 100k repeated values shared; exact Unicode/formatting/order/keys, input isolation and reload preserved");
 }
}
'@ | Set-Content "$output/Check.java" -Encoding utf8
$cp = Join-Path $root 'build/classes/java/main'
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if ($LASTEXITCODE -ne 0) { throw 'Translation check compilation failed' }
& "$Jdk/bin/java.exe" -Xverify:all -cp "$output;$cp" Check
if ($LASTEXITCODE -ne 0) { throw 'Translation check failed' }
$class = 'E:\机械动力魔法大冒险\.minecraft\versions\hkx\.mixin.out\class\net\minecraft\client\resources\language\ClientLanguage.class'
$code = (& "$Jdk/bin/javap.exe" -c -p $class) -join "`n"
if ($LASTEXITCODE -ne 0 -or ([regex]::Matches($code,'Method com/google/common/collect/ImmutableMap.copyOf:\(Ljava/util/Map;\)').Count -ne 2)) {
    throw 'Actual merged ClientLanguage snapshot sites changed'
}
Write-Output 'PASS: actual merged ClientLanguage has expected translation/component snapshot sites'
