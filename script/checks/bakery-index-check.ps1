param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/bakery-index-check'
New-Item -ItemType Directory -Force $output | Out-Null
$cp = (Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()
@'
import java.util.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.*;
import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
public class Check {
 static Instrumentation instrumentation;
 public static void premain(String args,Instrumentation value){instrumentation=value;}
 static class Loader extends ClassLoader {Class<?> define(byte[] data){return defineClass(null,data,0,data.length);}}
 public static void main(String[] args)throws Exception {
  String[] names={"unbakedCache","bakedCache","topLevelModels","bakedTopLevelModels"};
  var upstream=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(upstream,0);
  int sites=0;
  for(var m:upstream.methods)if(m.name.equals("<init>")) {
   var assigned=new HashSet<String>();
   for(var i:m.instructions) {
    if(i instanceof FieldInsnNode f && f.getOpcode()==Opcodes.PUTFIELD && List.of(names).contains(f.name))assigned.add(f.name);
    if(i instanceof FieldInsnNode f && f.getOpcode()==Opcodes.PUTFIELD && f.name.equals("bakedTopLevelModels")) {
     if(assigned.size()!=4)throw new AssertionError("Map initialization order");sites++;
    }
   }
  }
  if(sites!=1)throw new AssertionError("Constructor site");
  var hook=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[1]))).accept(hook,0);
  var fixture=new ClassNode();fixture.version=Opcodes.V21;fixture.access=Opcodes.ACC_PUBLIC;fixture.name="Fixture";fixture.superName="java/lang/Object";
  for(String name:names)fixture.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,name,"Ljava/util/Map;",null,null));
  var method=hook.methods.stream().filter(m->m.name.equals("gt$compactIndexes")).findFirst().orElseThrow();
  if(method.visibleAnnotations==null || method.visibleAnnotations.stream().noneMatch(a->a.desc.endsWith("/Inject;")))throw new AssertionError("Missing injection");
  method.access=Opcodes.ACC_PUBLIC;method.name="apply";method.desc="()V";method.visibleAnnotations=null;method.invisibleAnnotations=null;
  for(var i:method.instructions)if(i instanceof FieldInsnNode f && f.owner.equals(hook.name))f.owner="Fixture";
  fixture.methods.add(method);var writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);fixture.accept(writer);
  var type=new Loader().define(writer.toByteArray());
  var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
  Object instance=unsafe.allocateInstance(type);
  var initial=new LinkedHashMap<String,Object>();initial.put("seed",new Object());initial.put(null,null);
  for(String name:names)type.getField(name).set(instance,new HashMap<>(initial));
  type.getMethod("apply").invoke(instance);
  for(String name:names)if(!type.getField(name).get(instance).equals(initial))throw new AssertionError("Lost preexisting data");
  int[] counts={67431,91650,544421,544666};long before=0,after=0;
  for(int n=0;n<counts.length;n++) {
   Map<Object,Object> compact=(Map<Object,Object>)type.getField(names[n]).get(instance);compact.clear();
   Map<Object,Object> reference=new HashMap<>();Object value=new Object();
   for(int i=0;i<counts[n];i++){String key="model/"+i;reference.put(key,value);compact.put(key,value);}
   if(!compact.equals(reference) || compact.get(new String("model/130"))!=value)throw new AssertionError("Lookup identity");
   before+=structure(reference);after+=structure(compact);
   compact.put(null,value);compact.put("null-value",null);
   if(compact.get(null)!=value || compact.getOrDefault("null-value",value)!=null || compact.getOrDefault("missing",value)!=value)throw new AssertionError("Null/default");
   compact.compute("model/1",(k,v)->null);compact.computeIfAbsent("computed",k->value);
   compact.merge("computed",value,(a,b)->a);compact.replace("computed",null);
   var it=compact.entrySet().iterator();it.next();it.remove();
   for(var entry:compact.entrySet())if(entry.getValue()!=null && entry.getValue()!=value)throw new AssertionError("Changed value");
  }
  System.out.println("PASS: actual compiled hook executed; merged constructor site; 1248168 map entries identity/equals/null/compute/merge/mutation");
  System.out.println("Structure "+before+" -> "+after+" bytes; saved "+(before-after)+" (layout estimate, not new game heap)");
 }
 static long structure(Map<?,?> map)throws Exception {
  long bytes=instrumentation.getObjectSize(map);
  if(map instanceof Object2ObjectOpenHashMap<?,?>) {
   for(String name:List.of("key","value")){var f=Object2ObjectOpenHashMap.class.getDeclaredField(name);f.setAccessible(true);bytes+=instrumentation.getObjectSize(f.get(map));}
  } else {
   var f=HashMap.class.getDeclaredField("table");f.setAccessible(true);Object[] table=(Object[])f.get(map);bytes+=instrumentation.getObjectSize(table);
   f=Class.forName("java.util.HashMap$Node").getDeclaredField("next");f.setAccessible(true);
   for(Object node:table)while(node!=null){bytes+=instrumentation.getObjectSize(node);node=f.get(node);}
  }
  return bytes;
 }
}
'@ | Set-Content -Encoding UTF8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -cp $cp -d $output "$output/Check.java"
if ($LASTEXITCODE) {throw 'Compile failed'}
"Premain-Class: Check`n`n" | Set-Content -Encoding ascii "$output/manifest.mf"
& "$Jdk/bin/jar.exe" cfm "$output/agent.jar" "$output/manifest.mf" -C $output Check.class
if ($LASTEXITCODE) {throw 'Agent build failed'}
$merged = 'E:/机械动力魔法大冒险/.minecraft/versions/hkx/.mixin.out/class/net/minecraft/client/resources/model/ModelBakery.class'
$hook = "$root/build/classes/java/main/net/fodoth/skina/goldentweaks/mixin/fix/model/ModelBakeryIndexesMixin.class"
foreach ($layout in @('-XX:+UseCompressedOops','-XX:-UseCompressedOops')) {
 & "$Jdk/bin/java.exe" -Xmx2g $layout --add-opens=java.base/java.util=ALL-UNNAMED "-javaagent:$output/agent.jar" -Xverify:all -cp "$output;$cp" Check $merged $hook
 if ($LASTEXITCODE) {throw 'Bakery index check failed'}
}
