param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/thaum-styles-check'
New-Item -ItemType Directory -Force $output | Out-Null
$asm = foreach ($module in @('asm','asm-tree')) { Get-ChildItem "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/org.ow2.asm/$module" -Recurse -Filter "$module-9.8.jar" | Select-Object -First 1 -ExpandProperty FullName }
@'
import java.util.*;
import java.util.jar.*;
import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class Check {
 public static void main(String[] args) throws Exception {
  try(var jar=new JarFile(args[0])) {
   ClassNode renderer=read(jar,"thaumcraft/client/renderers/entity/LegacyThaumcraftMobRenderer");
   int creates=0,gets=0;
   for(var m:renderer.methods)for(var i:m.instructions) {
    if(i instanceof FieldInsnNode f && f.name.equals("styles") && f.getOpcode()==Opcodes.GETFIELD && !m.name.equals("style"))throw new AssertionError("New styles consumer: "+m.name);
    if(i instanceof MethodInsnNode call) {
     if(call.owner.endsWith("/MobRenderStyles") && call.name.equals("create")) {
      if(!m.name.equals("<init>") || !call.desc.equals("(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lnet/minecraft/client/renderer/entity/RenderLayerParent;Lnet/minecraft/client/model/EntityModel;)Ljava/util/Map;"))throw new AssertionError("Factory site changed");
      creates++;
     }
     if(m.name.equals("style") && call.owner.equals("java/util/Map") && call.name.equals("get"))gets++;
    }
   }
   if(creates!=1 || gets!=1)throw new AssertionError("Renderer access sites");
   for(var e:Collections.list(jar.entries()))if(e.getName().startsWith("thaumcraft/client/renderers/entity/mob/") && e.getName().endsWith(".class")) {
    var node=new ClassNode();new ClassReader(jar.getInputStream(e)).accept(node,0);
    for(var m:node.methods)for(var i:m.instructions)if(i instanceof MethodInsnNode call && call.name.equals("addLayer"))throw new AssertionError("Parent side effect: "+node.name);
   }
   var factory=read(jar,"thaumcraft/client/renderers/entity/mob/MobRenderStyles");
   Set<String> kinds=new LinkedHashSet<>();
   for(var m:factory.methods)if(m.name.equals("create"))for(var i:m.instructions)if(i instanceof FieldInsnNode f && f.owner.endsWith("/LegacyThaumcraftMobKind"))kinds.add(f.name);
   if(kinds.size()!=32)throw new AssertionError("Factory kinds: "+kinds.size());
   Path p=Path.of(args[1],"thaumcraft/common/entities/LegacyThaumcraftMobKind.java");Files.createDirectories(p.getParent());
   Files.writeString(p,"package thaumcraft.common.entities; public enum LegacyThaumcraftMobKind {"+String.join(",",kinds)+"}");
  }
  System.out.println("PASS: real renderer factory/get sites, 32 kinds, no parent addLayer side effects");
 }
 static ClassNode read(JarFile jar,String name)throws Exception {var n=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry(name+".class"))).accept(n,0);return n;}
}
'@ | Set-Content -Encoding UTF8 "$output/Check.java"
$cp = $asm -join ';'
& "$Jdk/bin/javac.exe" -cp $cp -d $output "$output/Check.java"
if ($LASTEXITCODE) {throw 'Compile failed'}
& "$Jdk/bin/java.exe" -cp "$output;$cp" Check "$root/libs/implementation/thaumcraft-0.3.0-port.280-scex.compat.1.jar" $output
if ($LASTEXITCODE) {throw 'Upstream check failed'}
$stubs = @{
 'net/minecraft/client/model/EntityModel.java' = 'package net.minecraft.client.model; public class EntityModel<T> {}'
 'net/minecraft/client/renderer/entity/EntityRendererProvider.java' = 'package net.minecraft.client.renderer.entity; public interface EntityRendererProvider { class Context {} }'
 'net/minecraft/client/renderer/entity/RenderLayerParent.java' = 'package net.minecraft.client.renderer.entity; public interface RenderLayerParent<T,M> {}'
 'thaumcraft/common/entities/LegacyThaumcraftMobEntity.java' = 'package thaumcraft.common.entities; public class LegacyThaumcraftMobEntity {}'
 'thaumcraft/client/renderers/entity/mob/MobRenderStyle.java' = 'package thaumcraft.client.renderers.entity.mob; public class MobRenderStyle { public Object parent,model; public int pose; public MobRenderStyle(Object p,Object m){parent=p;model=m;} }'
 'thaumcraft/client/renderers/entity/mob/MobRenderStyles.java' = 'package thaumcraft.client.renderers.entity.mob; import java.util.*; import thaumcraft.common.entities.*; import net.minecraft.client.renderer.entity.*; import net.minecraft.client.model.*; public class MobRenderStyles {public static int builds;public static Map<LegacyThaumcraftMobKind,MobRenderStyle> create(EntityRendererProvider.Context c,RenderLayerParent<LegacyThaumcraftMobEntity,EntityModel<LegacyThaumcraftMobEntity>> p,EntityModel<LegacyThaumcraftMobEntity> m){ builds++;var result=new EnumMap<LegacyThaumcraftMobKind,MobRenderStyle>(LegacyThaumcraftMobKind.class); for(var k:LegacyThaumcraftMobKind.values()) result.put(k,new MobRenderStyle(p,new Object()));return result;} }'
}
foreach ($entry in $stubs.GetEnumerator()) {$path=Join-Path $output $entry.Key;New-Item -ItemType Directory -Force (Split-Path $path) | Out-Null;Set-Content -Encoding UTF8 $path $entry.Value}
@'
import java.util.*;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.*;
import thaumcraft.common.entities.*;
import thaumcraft.client.renderers.entity.mob.*;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.client.GTLegacyMobStyles;
public class Behavior {
 public static void main(String[] args)throws Exception {
  var context=new EntityRendererProvider.Context();
  var parent=new RenderLayerParent<LegacyThaumcraftMobEntity,EntityModel<LegacyThaumcraftMobEntity>>(){};
  var model=new EntityModel<LegacyThaumcraftMobEntity>();
  var first=new GTLegacyMobStyles(context,parent,model);
  if(!first.isEmpty() || MobRenderStyles.builds!=0 || first.get(null)!=null || first.get("wrong")!=null)throw new AssertionError("Eager/invalid lookup");
  var kind=LegacyThaumcraftMobKind.values()[0];var style=first.get(kind);
  for(int i=0;i<10000;i++)if(first.get(kind)!=style)throw new AssertionError("Identity");
  if(first.size()!=1 || MobRenderStyles.builds!=1 || style.parent!=parent)throw new AssertionError("Retained size/parent");
  var second=new GTLegacyMobStyles(context,parent,model);var other=second.get(kind);
  style.pose=34;if(other==style || other.model==style.model || other.pose!=0)throw new AssertionError("Renderer isolation");
  var threads=new ArrayList<Thread>();
  for(int i=0;i<8;i++)threads.add(Thread.ofPlatform().start(()->{for(var k:LegacyThaumcraftMobKind.values())first.get(k);}));
  for(var thread:threads)thread.join();
  if(first.size()!=32 || MobRenderStyles.builds!=33 || second.size()!=1 || first.get(kind)!=style)throw new AssertionError("Multi-kind/concurrency");
  var reload=new GTLegacyMobStyles(new EntityRendererProvider.Context(),parent,model);
  if(!reload.isEmpty() || reload.get(kind)==style)throw new AssertionError("Reload isolation");
  System.out.println("PASS: compiled helper lazy allocation, one retained style, 32-kind fallback, identity, pose/model isolation, concurrency, reload");
 }
}
'@ | Set-Content -Encoding UTF8 "$output/Behavior.java"
$sources = Get-ChildItem $output -Recurse -Filter '*.java' | Where-Object Name -ne 'Check.java' | Select-Object -ExpandProperty FullName
& "$Jdk/bin/javac.exe" -cp "$root/build/classes/java/main" -d $output @sources
if ($LASTEXITCODE) {throw 'Behavior compile failed'}
& "$Jdk/bin/java.exe" -Xverify:all -cp "$output;$root/build/classes/java/main" Behavior
if ($LASTEXITCODE) {throw 'Behavior failed'}
