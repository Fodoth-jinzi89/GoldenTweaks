param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/connector-mapping-check'
New-Item -ItemType Directory -Force $output | Out-Null
$connector = Get-ChildItem -LiteralPath 'E:/机械动力魔法大冒险/.minecraft/versions/hkx/mods' -Filter '*connector-2*.jar' | Select-Object -First 1
$cp = (Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()+";$($connector.FullName)"
@'
import java.util.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.*;
import java.util.jar.*;
import reloc.net.minecraftforge.srgutils.*;
import net.fodoth.skina.goldentweaks.compat.connector.GTMappingStrings;
public class Check {
 static Instrumentation instrumentation;
 public static void premain(String args,Instrumentation value){instrumentation=value;}
 public static void main(String[] args)throws Exception {
  try(var jar=new JarFile(args[0]);var input=jar.getInputStream(jar.getJarEntry("mappings.tsrg"))) {
   var mappings=INamedMappingFile.load(input);
   var names=mappings.getNames();List<IMappingFile> maps=new ArrayList<>();
   maps.add(mappings.getMap("intermediary","mojang"));
   maps.add(mappings.getMap("mojang","intermediary"));
   var beforeMaps=new ArrayList<>(maps);long digest=0;
   for(var map:maps)digest=31*digest+check(map);
   long before=textSize(mappings);int replaced=GTMappingStrings.compact(mappings);long after=textSize(mappings);
   long newDigest=0;for(var map:maps)newDigest=31*newDigest+check(map);
   if(digest!=newDigest || !maps.equals(beforeMaps))throw new AssertionError("Mapping changed");
   if(mappings.getMap("intermediary","mojang")!=maps.get(0)||mappings.getMap("mojang","intermediary")!=maps.get(1))throw new AssertionError("Map identity");
   if(GTMappingStrings.compact(mappings)!=0)throw new AssertionError("Not idempotent");
   if(replaced==0 || after>=before)throw new AssertionError("No saving");
   var added=mappings.getMap("official","mojang");check(added);
   System.out.println("PASS: actual Connector two runtime directions/classes/fields/methods/descriptors; cache identity, missing names, idempotence and later new direction");
   System.out.println("Replaced "+replaced+" string references; reachable text layout "+before+" -> "+after+" bytes; saved "+(before-after));
  }
 }
 static long check(IMappingFile map) {
  long hash=0;
  for(var cls:map.getClasses()) {
   if(map.getClass(cls.getOriginal())!=cls || !map.remapClass(cls.getOriginal()).equals(cls.getMapped()))throw new AssertionError("Class lookup");
   hash=31*hash+Objects.hash(cls.getOriginal(),cls.getMapped());
   for(var field:cls.getFields()) {
    if(!cls.remapField(field.getOriginal()).equals(field.getMapped()))throw new AssertionError("Field lookup");
    hash=31*hash+Objects.hash(field.getOriginal(),field.getMapped(),field.getDescriptor(),field.getMappedDescriptor());
   }
   for(var method:cls.getMethods()) {
    if(!cls.remapMethod(method.getOriginal(),method.getDescriptor()).equals(method.getMapped()))throw new AssertionError("Method lookup");
    hash=31*hash+Objects.hash(method.getOriginal(),method.getMapped(),method.getDescriptor(),method.getMappedDescriptor());
   }
  }
  if(!map.remapClass("unknown/SomeClass").equals("unknown/SomeClass"))throw new AssertionError("Unknown class");
  return hash;
 }
 static long textSize(Object root)throws Exception {
  Set<Object> seen=Collections.newSetFromMap(new IdentityHashMap<>());ArrayDeque<Object> pending=new ArrayDeque<>();pending.add(root);long bytes=0;
  Field textValue=String.class.getDeclaredField("value");textValue.setAccessible(true);
  while(!pending.isEmpty()) {
   Object current=pending.removeLast();if(!seen.add(current))continue;
   if(current instanceof String text) {
    bytes+=instrumentation.getObjectSize(text);Object data=textValue.get(text);if(seen.add(data))bytes+=instrumentation.getObjectSize(data);
   } else if(current instanceof Map<?,?> map) {
    for(Object key:map.keySet())if(key!=null)pending.add(key);for(Object value:map.values())if(value!=null)pending.add(value);
   } else if(current instanceof Object[] array) {for(Object value:array)if(value!=null)pending.add(value);
   } else if(current.getClass().getName().startsWith("reloc.net.minecraftforge.srgutils.")) {
    for(Class<?> type=current.getClass();type!=null;type=type.getSuperclass())for(Field field:type.getDeclaredFields()) {
     if(Modifier.isStatic(field.getModifiers())||field.getType().isPrimitive())continue;field.setAccessible(true);Object value=field.get(current);if(value!=null)pending.add(value);
    }
   }
  }return bytes;
 }
}
'@ | Set-Content -Encoding UTF8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -cp $cp -d $output "$output/Check.java"
if($LASTEXITCODE){throw 'Compile failed'}
"Premain-Class: Check`n`n" | Set-Content -Encoding ascii "$output/manifest.mf"
& "$Jdk/bin/jar.exe" cfm "$output/agent.jar" "$output/manifest.mf" -C $output Check.class
if($LASTEXITCODE){throw 'Agent failed'}
foreach($layout in @('-XX:+UseCompressedOops','-XX:-UseCompressedOops')) {
 & "$Jdk/bin/java.exe" -Xmx2g $layout --add-opens=java.base/java.lang=ALL-UNNAMED "-javaagent:$output/agent.jar" -Xverify:all -cp "$output;$cp" Check $connector.FullName
 if($LASTEXITCODE){throw 'Mapping check failed'}
}
