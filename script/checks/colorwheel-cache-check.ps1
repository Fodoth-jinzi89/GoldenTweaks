param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/colorwheel-cache-check'
New-Item -ItemType Directory -Force $output | Out-Null
$mods = 'E:/机械动力魔法大冒险/.minecraft/versions/hkx/mods'
$colorwheel = Get-ChildItem -LiteralPath $mods -Filter '*colorwheel-1*.jar' | Select-Object -First 1
$iris = Get-ChildItem -LiteralPath $mods -Filter '*iris-*.jar' | Select-Object -First 1
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($iris.FullName)
try { [System.IO.Compression.ZipFileExtensions]::ExtractToFile($zip.GetEntry('META-INF/jarjar/glsl-transformer-3.0.0-pre3.jar'),"$output/glsl.jar",$true) } finally {$zip.Dispose()}
$antlr = Get-ChildItem "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/org.antlr/antlr4-runtime/4.13.1" -Recurse -Filter 'antlr4-runtime-4.13.1.jar' | Select-Object -First 1
$cp = (Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()+";$($colorwheel.FullName);$($iris.FullName);$output/glsl.jar;$($antlr.FullName)"
@'
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import dev.djefrey.colorwheel.compile.transform.ClrwlASTTransformer;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.parser.*;
import net.fodoth.skina.goldentweaks.compat.iris.GTGlslCaches;
public class Check {
 static class Owner { private static Object transformer; }
 public static void main(String[] args)throws Exception {
  try(var jar=new JarFile(args[0])) {
   var n=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry("dev/djefrey/colorwheel/compile/transform/ClrwlTransformPatcher.class"))).accept(n,0);
   if(n.fields.stream().noneMatch(f->f.name.equals("transformer") && f.desc.equals("Ldev/djefrey/colorwheel/compile/transform/ClrwlASTTransformer;") && (f.access&Opcodes.ACC_STATIC)!=0))throw new AssertionError("Owner field");
   var init=n.methods.stream().filter(m->m.name.equals("<clinit>")).findFirst().orElseThrow();
   int assigns=0,returns=0;for(var i:init.instructions){if(i instanceof FieldInsnNode f && f.name.equals("transformer") && f.getOpcode()==Opcodes.PUTSTATIC)assigns++;if(i.getOpcode()==Opcodes.RETURN)returns++;}
   if(assigns!=1 || returns!=1)throw new AssertionError("Owner initializer");
  }
  var transformer=new ClrwlASTTransformer<>();Owner.transformer=transformer;
  var parserField=ASTParser.class.getDeclaredField("parser");parserField.setAccessible(true);
  if(parserField.get(transformer) instanceof TwoTierCachingParser)throw new AssertionError("Expected upstream plain parser");
  GTGlslCaches.configureTwoTierOwner(Owner.class);
  var parser=(TwoTierCachingParser)parserField.get(transformer);
  var reference=new CachingParser();
  for(int round=0;round<3;round++)for(int i=0;i<28;i++) {
   String text="#version 330 core\nvoid main(){ float result="+i+".0; }";
   if(!parser.parse(text,ParseShape.TRANSLATION_UNIT).getText().equals(reference.parse(text,ParseShape.TRANSLATION_UNIT).getText()))throw new AssertionError("Changed translation unit");
   text="float function"+i+"(){return "+i+".0;}";
   if(!parser.parse(text,ParseShape.EXTERNAL_DECLARATION).getText().equals(reference.parse(text,ParseShape.EXTERNAL_DECLARATION).getText()))throw new AssertionError("Changed declaration");
  }
  var secondary=TwoTierCachingParser.class.getDeclaredField("secondaryCache");secondary.setAccessible(true);
  var primary=CachingParser.class.getDeclaredField("parseCache");primary.setAccessible(true);
  Map full=(Map)secondary.get(parser),small=(Map)primary.get(parser);
  if(full.size()!=2 || small.size()!=28)throw new AssertionError("Tier separation "+full.size()+"/"+small.size());
  for(int i=0;i<100;i++)parser.parse("float name"+i+";",ParseShape.EXTERNAL_DECLARATION);
  if(small.size()!=64)throw new AssertionError("Primary limit");
  var build=ASTParser.class.getDeclaredField("buildCache");build.setAccessible(true);Map cache=(Map)build.get(transformer);
  for(int i=0;i<1000;i++)cache.put(i,i);if(cache.size()!=64)throw new AssertionError("Build limit");
  GTGlslCaches.configureTwoTierOwner(Owner.class);
  if(parserField.get(transformer)==parser || !((Map)build.get(transformer)).isEmpty())throw new AssertionError("Reconfigure");
  Class<?> actualOwner=Class.forName("dev.djefrey.colorwheel.compile.transform.ClrwlTransformPatcher");
  GTGlslCaches.configureTwoTierOwner(actualOwner);
  var field=actualOwner.getDeclaredField("transformer");field.setAccessible(true);
  if(!(parserField.get(field.get(null)) instanceof TwoTierCachingParser))throw new AssertionError("Actual owner configuration");
  System.out.println("PASS: real Colorwheel transformer switched plain -> two-tier; 168 real parse comparisons across eviction, 64/64/2 bounds, reset; actual owner initializer and field");
 }
}
'@ | Set-Content -Encoding UTF8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -cp $cp -d $output "$output/Check.java"
if ($LASTEXITCODE) {throw 'Compile failed'}
& "$Jdk/bin/java.exe" -Xverify:all -cp "$output;$cp" Check $colorwheel.FullName
if ($LASTEXITCODE) {throw 'Colorwheel cache check failed'}
