param([string]$Jdk='C:/Program Files/BellSoft/LibericaJDK-21')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$output=Join-Path $root 'build/mixin-tree-text-check'
New-Item -ItemType Directory -Force $output | Out-Null
$cp=(Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()
@'
import java.util.*;
import java.util.jar.*;
import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import net.fodoth.skina.goldentweaks.util.GTMixinTreeText;
public class Check {
 static byte[] bytes(ClassNode node){var writer=new ClassWriter(0);node.accept(writer);return writer.toByteArray();}
 public static void main(String[] args)throws Exception {
  var roots=new ArrayList<ClassNode>();var before=new ArrayList<byte[]>();
  for(String path:args)try(var jar=new JarFile(path)) {
   for(var entry:Collections.list(jar.entries()))if(entry.getName().endsWith(".class")) {
    var node=new ClassNode();new ClassReader(jar.getInputStream(entry)).accept(node,0);
    roots.add(node);before.add(bytes(node));
   }
  }
  var oldMethods=new ArrayList<MethodNode>();var oldInsns=new ArrayList<AbstractInsnNode>();
  for(var node:roots)for(var method:node.methods){oldMethods.add(method);for(var insn:method.instructions)oldInsns.add(insn);}
  int replaced=GTMixinTreeText.compact(roots);
  if(replaced<1000)throw new AssertionError("No meaningful sharing: "+replaced);
  int m=0,n=0;
  for(int i=0;i<roots.size();i++) {
   ClassNode root=roots.get(i);
   if(!Arrays.equals(before.get(i),bytes(root)))throw new AssertionError("Bytecode changed: "+root.name);
   var clone=new ClassNode();root.accept(clone);
   if(!Arrays.equals(before.get(i),bytes(clone)))throw new AssertionError("Later template clone changed");
   for(var method:root.methods){if(method!=oldMethods.get(m++))throw new AssertionError("Method identity");for(var insn:method.instructions)if(insn!=oldInsns.get(n++))throw new AssertionError("Instruction identity");}
  }
  if(GTMixinTreeText.compact(roots)!=0)throw new AssertionError("Not idempotent");
  var a=new ClassNode();var b=new ClassNode();a.name=new String("same/Name");b.name=new String("same/Name");
  GTMixinTreeText.compact(List.of(a,b));if(a.name!=b.name)throw new AssertionError("Text not shared");
  a.name="changed/Name";if(!b.name.equals("same/Name"))throw new AssertionError("Node mutation leaked");
  if(GTMixinTreeText.compact(List.of())!=0)throw new AssertionError("Empty");
  // Validate reflection field names against the actual runtime library without initializing Mixin.
  ClassLoader loader=Check.class.getClassLoader();
  for(String[] pair:new String[][]{{"Config","allConfigs"},{"Config","config"},{"MixinConfig","mixins"},{"MixinInfo","state"},{"MixinInfo$State","classNode"},{"MixinInfo$State","validationClassNode"}})
   Class.forName("org.spongepowered.asm.mixin.transformer."+pair[0],false,loader).getDeclaredField(pair[1]);
  System.out.println("PASS: "+roots.size()+" real classes, "+m+" methods, "+n+" instructions; "+replaced+" text references shared; exact bytes and later clone bytes unchanged; identities, mutation isolation, idempotence, runtime reflection fields");
 }
}
'@ | Set-Content -Encoding utf8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if($LASTEXITCODE){throw 'Compile failed'}
$mods='E:/机械动力魔法大冒险/.minecraft/versions/hkx/mods'
$jars=@("$root/build/libs/goldentweaks-4.7.8.jar")
$jars+=@(Get-ChildItem -LiteralPath $mods -Filter '*.jar' | Where-Object {$_.Name -match 'continuity|iris-neoforge|sodium-neoforge'} | ForEach-Object {$_.FullName})
& "$Jdk/bin/java.exe" -Xmx3g -Xverify:all -cp "$output;$cp" Check @jars
if($LASTEXITCODE){throw 'Template text check failed'}
