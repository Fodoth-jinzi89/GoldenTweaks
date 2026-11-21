param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/continuity-model-check'
New-Item -ItemType Directory -Force $output | Out-Null
@'
allprojects {
 tasks.register('gtWriteCheckClasspath') {
  doLast { file('build/continuity-runtime-classpath.txt').text = sourceSets.main.runtimeClasspath.asPath }
 }
}
'@ | Set-Content -Encoding UTF8 "$output/classpath.gradle"
Push-Location $root
try {
 & "$root/gradlew.bat" -I "$output/classpath.gradle" gtWriteCheckClasspath --no-configuration-cache --console=plain
 if ($LASTEXITCODE) {throw 'Classpath failed'}
} finally {Pop-Location}
$cp = (Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()
@'
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.fodoth.skina.goldentweaks.compat.continuity.GTBlockStateModelLookup;
public class Check {
 public static void main(String[] args) {
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
  int checked=0;
  for(var block:BuiltInRegistries.BLOCK)for(var state:block.getStateDefinition().getPossibleStates()) {
   var id=BlockModelShaper.stateToModelLocation(state);
   if(GTBlockStateModelLookup.find(id)!=state)throw new AssertionError("State mismatch: "+id);
   if(GTBlockStateModelLookup.find(new ModelResourceLocation(id.id(),id.getVariant()+","))!=null)throw new AssertionError("Trailing comma");
   String[] entries=id.getVariant().split(",");
   if(entries.length>1) {
    var reversed=new ArrayList<>(List.of(entries));Collections.reverse(reversed);
    if(GTBlockStateModelLookup.find(new ModelResourceLocation(id.id(),String.join(",",reversed)))!=null)throw new AssertionError("Noncanonical order");
    if(GTBlockStateModelLookup.find(new ModelResourceLocation(id.id(),entries[0]))!=null)throw new AssertionError("Partial state");
   }
   checked++;
  }
  for(String variant:List.of("inventory","foo=bar","=x","waterlogged=invalid","facing=north,facing=north"))
   if(GTBlockStateModelLookup.find(new ModelResourceLocation(ResourceLocation.withDefaultNamespace("oak_stairs"),variant))!=null)throw new AssertionError("Invalid variant: "+variant);
  if(GTBlockStateModelLookup.find(null)!=null || GTBlockStateModelLookup.find("wrong")!=null || GTBlockStateModelLookup.find(new ModelResourceLocation(ResourceLocation.parse("gt_missing:block"),""))!=null)throw new AssertionError("Unknown key");
  System.out.println("PASS: "+checked+" real registry states round-trip by identity; noncanonical/partial/invalid/unknown IDs rejected");
 }
}
'@ | Set-Content -Encoding UTF8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if ($LASTEXITCODE) {throw 'Compile failed'}
& "$Jdk/bin/java.exe" -Xmx2g -Xverify:all -cp "$output;$cp" Check
if ($LASTEXITCODE) {throw 'Registry check failed'}
$continuity = Get-ChildItem -LiteralPath "$root/libs" -Recurse -Filter '*continuity*.jar' | Select-Object -First 1
if (!$continuity) { $continuity = Get-ChildItem -LiteralPath 'E:/机械动力魔法大冒险/.minecraft/versions/hkx/mods' -Filter '*continuity*.jar' | Select-Object -First 1 }
if (!$continuity) {throw 'Continuity jar required for injection checks'}
$code = (& "$Jdk/bin/javap.exe" -p -c -classpath $continuity.FullName me.pepperbell.continuity.client.resource.ModelWrappingHandler) -join "`n"
if ($LASTEXITCODE) {throw 'Upstream bytecode read failed'}
if ([regex]::Matches($code,'// Method createBlockStateModelIdMap:\(\)Lcom/google/common/collect/ImmutableMap;').Count -ne 1) {throw 'Constructor factory site changed'}
if ([regex]::Matches($code,'// Method com/google/common/collect/ImmutableMap.get:\(Ljava/lang/Object;\)Ljava/lang/Object;').Count -ne 1) {throw 'Wrap lookup site changed'}
if ([regex]::Matches($code,'// Field blockStateModelIds:').Count -ne 2) {throw 'State index consumers changed'}
$wrap = $code.Substring($code.IndexOf('public net.minecraft.client.resources.model.BakedModel wrap('))
$reads = [regex]::Matches($wrap,'// Field blockStateModelIds:')
if ($reads.Count -ne 1 -or !$wrap.Contains('class me/pepperbell/continuity/client/model/CtmBakedModel') -or !$wrap.Contains('class me/pepperbell/continuity/client/model/EmissiveBakedModel')) {throw 'Wrapping path changed'}
Write-Output 'PASS: actual Continuity constructor factory and wrap lookup descriptors; sole index read stays inside original CTM/emissive wrapping path'
