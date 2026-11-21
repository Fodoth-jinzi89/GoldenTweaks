param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/block-texture-memory-check'
New-Item -ItemType Directory -Force $output | Out-Null
$cp = (Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()
@'
import java.util.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.*;
import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import com.google.gson.*;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
public class Check {
 static Instrumentation instrumentation;
 public static void premain(String args,Instrumentation value){instrumentation=value;}
 static class Loader extends ClassLoader {Class<?> define(byte[] data){return defineClass(null,data,0,data.length);}}
 public static void main(String[] args)throws Exception {
  var n=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(n,0);
  var fixture=new ClassNode();fixture.version=Opcodes.V21;fixture.access=Opcodes.ACC_PUBLIC;fixture.name="TextureFixture";fixture.superName="java/lang/Object";
  var hook=n.methods.stream().filter(m->m.name.equals("gt$compactTextures")).findFirst().orElseThrow();hook.access=Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC;hook.name="apply";
  // This handler does not use this; shift its original local indices for static execution.
  for(var i:hook.instructions)if(i instanceof VarInsnNode v)v.var--;
  hook.localVariables=null;hook.visibleAnnotations=null;hook.invisibleAnnotations=null;
  fixture.methods.add(hook);var w=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);fixture.accept(w);
  var apply=new Loader().define(w.toByteArray()).getMethod("apply",CallbackInfoReturnable.class);
  var upstream=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[1]))).accept(upstream,0);
  var parser=upstream.methods.stream().filter(m->m.name.equals("getTextureMap")).findFirst().orElseThrow();
  int returns=0;for(var i:parser.instructions)if(i.getOpcode()==Opcodes.ARETURN)returns++;if(returns!=1)throw new AssertionError("Parser return sites");
  Class<?> deserializer=Class.forName("net.minecraft.client.renderer.block.model.BlockModel$Deserializer");
  var constructor=deserializer.getDeclaredConstructor();constructor.setAccessible(true);Object instance=constructor.newInstance();
  var parse=deserializer.getDeclaredMethod("getTextureMap",JsonObject.class);parse.setAccessible(true);
  for(String json:List.of("{}","{\"textures\":{\"all\":\"minecraft:block/stone\",\"particle\":\"#all\"}}","{\"textures\":{\"a\":\"#b\",\"b\":\"#a\"}}")) {
   Map original=(Map)parse.invoke(instance,JsonParser.parseString(json).getAsJsonObject());Map result=compact(apply,original);
   if(!original.equals(result))throw new AssertionError("Parsed content");
   for(Object key:original.keySet())if(original.get(key)!=result.get(key))throw new AssertionError("Copied material/reference");
   result.put("extra",original.values().stream().findFirst().orElse(null));result.remove("extra");
  }
  Map empty=new HashMap();if(compact(apply,empty)!=empty)throw new AssertionError("Empty map");
  Map custom=new LinkedHashMap();custom.put("a","b");if(compact(apply,custom)!=custom)throw new AssertionError("Custom map");
  Map nullable=new HashMap();nullable.put(null,null);var nullableResult=compact(apply,nullable);
  if(!nullableResult.containsKey(null)||nullableResult.getOrDefault(null,"fallback")!=null)throw new AssertionError("Null semantics");
  var model=BlockModel.fromString("{\"textures\":{\"all\":\"minecraft:block/stone\",\"particle\":\"#all\"}}");
  if(!model.textureMap.get("particle").right().orElseThrow().equals("all"))throw new AssertionError("Reference parsing");
  if(args.length>2) {
   long before=0,after=0;int count=0;
   for(String line:Files.readAllLines(Path.of(args[2]))) {
    String[] values=line.split("\\s+");int size=Integer.parseInt(values[0]),repeats=Integer.parseInt(values[2]);if(size==0)continue;
    Map source=new HashMap();for(int i=0;i<size;i++)source.put("texture"+i,new Object());
    Map result=compact(apply,source);before+=structure(source)*repeats;after+=structure(result)*repeats;count+=repeats;
   }
   System.out.println("Layout estimate "+count+" nonempty maps: "+before+" -> "+after+" bytes; saved "+(before-after));
  }
  System.out.println("PASS: compiled handler, actual parser descriptors/JSON/material identity/reference semantics; mutable/null/empty/custom-map paths");
 }
 static Map compact(Method apply,Map map)throws Exception {var callback=new CallbackInfoReturnable<Map>("test",true,map);apply.invoke(null,callback);return callback.getReturnValue();}
 static long structure(Map map)throws Exception {
  long bytes=instrumentation.getObjectSize(map);
  if(map instanceof it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap) {
   for(String name:List.of("key","value")){var f=map.getClass().getDeclaredField(name);f.setAccessible(true);bytes+=instrumentation.getObjectSize(f.get(map));}
  } else {
   var f=HashMap.class.getDeclaredField("table");f.setAccessible(true);Object[] table=(Object[])f.get(map);bytes+=instrumentation.getObjectSize(table);
   f=Class.forName("java.util.HashMap$Node").getDeclaredField("next");f.setAccessible(true);for(Object node:table)while(node!=null){bytes+=instrumentation.getObjectSize(node);node=f.get(node);}
  }return bytes;
 }
}
'@ | Set-Content -Encoding UTF8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -cp $cp -d $output "$output/Check.java"
if ($LASTEXITCODE) {throw 'Compile failed'}
"Premain-Class: Check`n`n" | Set-Content -Encoding ascii "$output/manifest.mf"
& "$Jdk/bin/jar.exe" cfm "$output/agent.jar" "$output/manifest.mf" -C $output Check.class
if ($LASTEXITCODE) {throw 'Agent failed'}
$hook="$root/build/classes/java/main/net/fodoth/skina/goldentweaks/mixin/fix/model/BlockModelDeserializerMixin.class"
$merged='E:/机械动力魔法大冒险/.minecraft/versions/hkx/.mixin.out/class/net/minecraft/client/renderer/block/model/BlockModel$Deserializer.class'
$sizes='E:/tmp/heap-analysis-20261117-third/block-texture-sizes.tsv'
[string[]]$extra=@();if(Test-Path $sizes){$extra=@($sizes)}
foreach($layout in @('-XX:+UseCompressedOops','-XX:-UseCompressedOops')) {
 & "$Jdk/bin/java.exe" -Xmx2g $layout --add-opens=java.base/java.util=ALL-UNNAMED "-javaagent:$output/agent.jar" -Xverify:all -cp "$output;$cp" Check $hook $merged @extra
 if($LASTEXITCODE){throw 'Texture check failed'}
}
