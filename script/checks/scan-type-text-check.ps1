param([string]$Jdk = 'C:/Program Files/BellSoft/LibericaJDK-21')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$output=Join-Path $root 'build/scan-type-text-check'
New-Item -ItemType Directory -Force $output | Out-Null
$cp=(Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()
@'
import java.util.*;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import org.objectweb.asm.Type;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.fodoth.skina.goldentweaks.util.GTScanDataCompactor;
public class Check {
 public static void main(String[] args)throws Exception {
  var scan=new ModFileScanData();var types=new ArrayList<Type>();
  for(int i=0;i<160000;i++) {
   Type clazz=Type.getObjectType(new String("sample/Type"+i));
   Type parent=Type.getObjectType(new String("java/lang/Object"));
   Type iface=Type.getType(new String("Lsample/Interface"+(i%200)+";"));
   Type annotation=Type.getType(new String("Ljava/lang/Deprecated;"));
   Type annotationClass=Type.getObjectType(new String("sample/Type"+i));
   scan.getClasses().add(new ModFileScanData.ClassData(clazz,parent,Set.of(iface)));
   scan.getAnnotations().add(new ModFileScanData.AnnotationData(annotation,ElementType.TYPE,annotationClass,"member"+i,new HashMap<>()));
   types.addAll(List.of(clazz,parent,iface,annotation,annotationClass));
  }
  for(String descriptor:List.of("I","[[Ljava/lang/String;","(ILjava/lang/String;)Ljava/util/List;")) {
   Type type=Type.getType(descriptor);types.add(type);
   scan.getClasses().add(new ModFileScanData.ClassData(type,null,Set.of()));
  }
  String[] descriptors=types.stream().map(Type::getDescriptor).toArray(String[]::new);
  int[] hashes=types.stream().mapToInt(Type::hashCode).toArray();
  var classes=new ArrayList<>(scan.getClasses());var annotations=new ArrayList<>(scan.getAnnotations());
  Field buffer=Type.class.getDeclaredField("valueBuffer");buffer.setAccessible(true);
  int before=countBuffers(types,buffer);
  int shared=GTScanDataCompactor.compactTypeText(List.of(scan));int after=countBuffers(types,buffer);
  if(shared<600000 || before-after<600000)throw new AssertionError("Sharing failed: "+shared+" "+before+" -> "+after);
  for(int i=0;i<types.size();i++)if(!types.get(i).getDescriptor().equals(descriptors[i]) || types.get(i).hashCode()!=hashes[i])throw new AssertionError("Type changed");
  int i=0;for(var data:scan.getClasses()){if(data!=classes.get(i++))throw new AssertionError("Class record replaced");if(!scan.getClasses().contains(data))throw new AssertionError("Set hash broken");}
  i=0;for(var data:scan.getAnnotations()){if(data!=annotations.get(i++))throw new AssertionError("Annotation record replaced");if(!scan.getAnnotations().contains(data))throw new AssertionError("Annotation hash broken");}
  if(scan.getAnnotatedBy(Deprecated.class,ElementType.TYPE).count()!=160000)throw new AssertionError("Lookup broken");
  if(classes.getFirst().clazz()!=types.getFirst() || !classes.getFirst().interfaces().contains(types.get(2)))throw new AssertionError("Type identity changed");
  if(GTScanDataCompactor.compactTypeText(List.of(scan))!=0)throw new AssertionError("Not idempotent");
  if(GTScanDataCompactor.compactTypeText(List.of(new ModFileScanData()))!=0)throw new AssertionError("Empty scan");
  annotations.getFirst().annotationData().put("mutable",true);
  if(!scan.getAnnotations().iterator().next().annotationData().containsKey("mutable"))throw new AssertionError("Values replaced");
  System.out.println("PASS: 320003 real scan records; type/record identity, sliced descriptors, hash membership, annotation queries, mutation, null parent, primitive/array/method types, idempotence; "+before+" -> "+after+" distinct backing strings");
 }
 static int countBuffers(List<Type> types,Field field)throws Exception {
  Set<Object> seen=Collections.newSetFromMap(new IdentityHashMap<>());
  for(Type type:types){Object value=field.get(type);if(value!=null)seen.add(value);}
  return seen.size();
 }
}
'@ | Set-Content -Encoding utf8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if($LASTEXITCODE){throw 'Compile failed'}
& "$Jdk/bin/java.exe" -Xmx2g -Xverify:all -cp "$output;$cp" Check
if($LASTEXITCODE){throw 'Scan text check failed'}
