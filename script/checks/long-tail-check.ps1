param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/long-tail-check'
New-Item -ItemType Directory -Force $output | Out-Null
$cache = "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1"
$guava = Get-ChildItem "$cache/com.google.guava/guava/33.0.0-jre" -Recurse -Filter 'guava-33.0.0-jre.jar' | Select-Object -First 1
$antlr = Get-ChildItem "$cache/org.antlr/antlr4-runtime/4.13.1" -Recurse -Filter 'antlr4-runtime-4.13.1.jar' | Select-Object -First 1
$asm = foreach ($module in @('asm','asm-tree')) { Get-ChildItem "$cache/org.ow2.asm/$module" -Recurse -Filter "$module-9.8.jar" | Select-Object -First 1 -ExpandProperty FullName }
$mods = 'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods'
$iris = Get-ChildItem -LiteralPath $mods -Filter '*iris-*.jar' | Select-Object -First 1
$fancy = Get-ChildItem -LiteralPath $mods -Filter '*fancymenu-*.jar' | Select-Object -First 1
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($iris.FullName)
try { [System.IO.Compression.ZipFileExtensions]::ExtractToFile($zip.GetEntry('META-INF/jarjar/glsl-transformer-3.0.0-pre3.jar'),"$output/glsl.jar",$true) } finally {$zip.Dispose()}
$cp = (@("$root/build/classes/java/main","$output/glsl.jar",$guava.FullName,$antlr.FullName)+$asm)-join ';'
@'
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class Check {
 static class Loader extends ClassLoader {
  Loader(){super(Check.class.getClassLoader());}
  Class<?> define(byte[] data){return defineClass(null,data,0,data.length);}
 }
 public static void main(String[] args) throws Exception {
  try(JarFile irisJar=new JarFile(args[1])) {
   ClassNode owner=new ClassNode();new ClassReader(irisJar.getInputStream(irisJar.getJarEntry("net/irisshaders/iris/pipeline/transform/TransformPatcher.class"))).accept(owner,0);
   if(owner.fields.stream().noneMatch(f->f.name.equals("transformer") && (f.access&Opcodes.ACC_STATIC)!=0 && f.desc.equals("Lio/github/douira/glsl_transformer/ast/transform/EnumASTTransformer;")))throw new AssertionError("Iris owner field changed");
   var init=owner.methods.stream().filter(m->m.name.equals("<clinit>")).findFirst().orElseThrow();
   int assignments=0,returns=0;
   for(AbstractInsnNode ins:init.instructions){if(ins instanceof FieldInsnNode f && f.getOpcode()==Opcodes.PUTSTATIC && f.name.equals("transformer"))assignments++;if(ins.getOpcode()==Opcodes.RETURN)returns++;}
   if(assignments!=1 || returns!=1)throw new AssertionError("Iris clinit site changed");
  }
  var ast=new io.github.douira.glsl_transformer.ast.transform.ASTParser();
  ast.setParsingCacheStrategy(io.github.douira.glsl_transformer.ast.transform.ASTParser.ParsingCacheStrategy.TWO_TIER);
  var helper=net.fodoth.skina.goldentweaks.compat.iris.GTGlslCaches.class;
  net.fodoth.skina.goldentweaks.compat.iris.GTGlslCaches.configure(ast);
  var astType=io.github.douira.glsl_transformer.ast.transform.ASTParser.class;
  var buildField=astType.getDeclaredField("buildCache");buildField.setAccessible(true);
  Map cache=(Map)buildField.get(ast);
  for(int i=0;i<1000;i++)cache.put(i,i);
  if(cache.size()!=64||cache.containsKey(0)||!cache.containsKey(999))throw new AssertionError("AST LRU bound");
  cache.get(936);cache.put(1000,1000);if(!cache.containsKey(936)||cache.containsKey(937))throw new AssertionError("LRU ordering");
  var parserField=astType.getDeclaredField("parser");parserField.setAccessible(true);
  var parser=(io.github.douira.glsl_transformer.parser.TwoTierCachingParser)parserField.get(ast);
  var reference=new io.github.douira.glsl_transformer.parser.TwoTierCachingParser();
  for(int round=0;round<3;round++)for(int i=0;i<12;i++) {
   String source="#version 330 core\nvoid main(){float v="+i+".0;}";
   var a=parser.parse(source,io.github.douira.glsl_transformer.parser.ParseShape.TRANSLATION_UNIT);
   var b=reference.parse(source,io.github.douira.glsl_transformer.parser.ParseShape.TRANSLATION_UNIT);
   if(!a.getText().equals(b.getText()))throw new AssertionError("Parse result changed");
  }
  var secondary=parser.getClass().getDeclaredField("secondaryCache");secondary.setAccessible(true);
  if(((Map)secondary.get(parser)).size()!=2)throw new AssertionError("Translation LRU bound");
  var primary=io.github.douira.glsl_transformer.parser.CachingParser.class.getDeclaredField("parseCache");primary.setAccessible(true);
  Map primaryCache=(Map)primary.get(parser);for(int i=0;i<1000;i++)primaryCache.put(i,i);
  if(primaryCache.size()!=64)throw new AssertionError("Primary bound");
  var plain=new io.github.douira.glsl_transformer.ast.transform.ASTParser();
  net.fodoth.skina.goldentweaks.compat.iris.GTGlslCaches.configure(plain);
  class Owner {private static Object transformer;} Owner.transformer=ast;
  net.fodoth.skina.goldentweaks.compat.iris.GTGlslCaches.configureOwner(Owner.class);
  if(!((Map)buildField.get(ast)).isEmpty() || !((Map)secondary.get(parser)).isEmpty())throw new AssertionError("Owner reset path");
  var hook=Class.forName("net.fodoth.skina.goldentweaks.mixin.fix.fancymenu.ClientResourceIndexBuilderMixin").getDeclaredMethod("gt$compactResourceIndex",Set.class);hook.setAccessible(true);
  var ordered=new LinkedHashSet<String>();for(int i=0;i<10000;i++)ordered.add("resource/"+i);
  Set compact=(Set)hook.invoke(null,ordered);
  if(!new ArrayList<>(compact).equals(new ArrayList<>(ordered))||!compact.equals(ordered))throw new AssertionError("Resource order/content changed");
  ordered.clear();if(compact.size()!=10000||!compact.contains("resource/1234"))throw new AssertionError("Snapshot isolation");
  try{compact.remove("resource/1234");throw new AssertionError("Mutable index");}catch(UnsupportedOperationException expected){}
  try(JarFile jar=new JarFile(args[0])) {
   ClassNode n=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry("de/keksuccino/fancymenu/util/resource/ClientResourceIndexBuilder.class"))).accept(n,0);
   int sites=0;for(MethodNode m:n.methods)if(m.name.equals("build"))for(AbstractInsnNode ins:m.instructions)
    if(ins instanceof MethodInsnNode call&&call.owner.equals("java/util/Collections")&&call.name.equals("unmodifiableSet"))sites++;
   if(sites!=1)throw new AssertionError("Resource snapshot injection site changed");
  }
  System.out.println("PASS: compiled owner helper configures actual ASTParser/TwoTier public APIs; 64/64/2 bounds and LRU recency; 36 real parse comparisons after eviction; normal parser and static owner paths; resource snapshot checks");
 }
}
'@ | Set-Content "$output/Check.java" -Encoding utf8
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
& "$Jdk/bin/java.exe" -Xverify:all -cp "$output;$cp" Check $fancy.FullName $iris.FullName
if ($LASTEXITCODE -ne 0) { throw 'Long-tail cache checks failed' }
