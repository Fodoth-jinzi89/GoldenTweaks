param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/scan-data-memory-check'
New-Item -ItemType Directory -Force $output | Out-Null
$cp = (Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()
@'
import java.util.*;
import java.lang.annotation.ElementType;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.*;
import java.nio.file.*;
import org.objectweb.asm.Type;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.fodoth.skina.goldentweaks.util.GTScanDataCompactor;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
public class Check {
 static Instrumentation instrumentation;
 public static void premain(String args,Instrumentation value){instrumentation=value;}
 public static void main(String[] args)throws Exception {
  var scan=new ModFileScanData();
  for(int i=0;i<160000;i++) {
   var type=Type.getObjectType("test/Type"+i);
   scan.getClasses().add(new ModFileScanData.ClassData(type,Type.getType(Object.class),Set.of()));
   scan.getAnnotations().add(new ModFileScanData.AnnotationData(Type.getType(Deprecated.class),ElementType.TYPE,type,"entry"+i,new HashMap<>(Map.of("value",i))));
  }
  var oldClasses=scan.getClasses();var oldAnnotations=scan.getAnnotations();
  var classes=new ArrayList<>(oldClasses);var annotations=new ArrayList<>(oldAnnotations);
  long before=structure(oldClasses)+structure(oldAnnotations);
  if(GTScanDataCompactor.compact(List.of(scan))!=2)throw new AssertionError("Not compacted");
  long after=structure(scan.getClasses())+structure(scan.getAnnotations());
  checkIdentity(classes,new ArrayList<>(scan.getClasses()));checkIdentity(annotations,new ArrayList<>(scan.getAnnotations()));
  if(!scan.getClasses().equals(oldClasses) || !scan.getAnnotations().equals(oldAnnotations) || oldClasses.size()!=160000)throw new AssertionError("Content changed");
  if(scan.getAnnotatedBy(Deprecated.class,ElementType.TYPE).count()!=160000)throw new AssertionError("Annotation query");
  if(scan.getAnnotatedBy(Deprecated.class,ElementType.FIELD).count()!=0)throw new AssertionError("Target filter");
  var entry=classes.get(11);
  if(!scan.getClasses().contains(new ModFileScanData.ClassData(entry.clazz(),entry.parent(),entry.interfaces())))throw new AssertionError("Record equality");
  scan.getClasses().remove(entry);scan.getClasses().add(entry);
  if(new ArrayList<>(scan.getClasses()).getLast()!=entry)throw new AssertionError("Reinsert order");
  var it=scan.getClasses().iterator();var removed=it.next();it.remove();
  if(scan.getClasses().contains(removed))throw new AssertionError("Iterator remove");
  scan.getClasses().add(null);if(!scan.getClasses().contains(null))throw new AssertionError("Null");scan.getClasses().remove(null);
  if(GTScanDataCompactor.compact(List.of(scan))!=0)throw new AssertionError("Not idempotent");
  scan.getAnnotations().iterator().next().annotationData().put("extra",true);
  if(!oldAnnotations.iterator().next().annotationData().containsKey("extra"))throw new AssertionError("Record copied");
  var empty=new ModFileScanData();var emptyClasses=empty.getClasses();
  if(GTScanDataCompactor.compact(List.of(empty))!=0 || empty.getClasses()!=emptyClasses)throw new AssertionError("Empty changed");
  Field f=ModFileScanData.class.getDeclaredField("classes");f.setAccessible(true);
  Set<ModFileScanData.ClassData> custom=new HashSet<>();custom.add(entry);f.set(empty,custom);
  if(GTScanDataCompactor.compact(List.of(empty))!=0 || empty.getClasses()!=custom)throw new AssertionError("Custom set changed");
  if(after>=before)throw new AssertionError("No structural reduction");
  System.out.println("PASS: 320000 real scan records identity/order/equality/query/mutation/iterator/null/idempotence/custom-set checks");
  System.out.println("Structure for 320000 entries: "+before+" -> "+after+" bytes; saved "+(before-after));
  if(args.length>0) {
   long oldSize=0,newSize=0;int count=0;
   for(String line:Files.readAllLines(Path.of(args[0])))for(String value:line.split("\\s+")) {
    int n=Integer.parseInt(value);if(n==0)continue;
    Set<Object> a=new LinkedHashSet<>();for(int i=0;i<n;i++)a.add(new Object());
    Set<Object> b=new ObjectLinkedOpenHashSet<>(a);
    oldSize+=structure(a);newSize+=structure(b);count+=n;
   }
   System.out.println("Heap-size distribution "+count+" entries: structure "+oldSize+" -> "+newSize+"; saved "+(oldSize-newSize)+" bytes (layout estimate, not game heap measured)");
  }
 }
 static void checkIdentity(List<?> a,List<?> b){if(a.size()!=b.size())throw new AssertionError("Size");for(int i=0;i<a.size();i++)if(a.get(i)!=b.get(i))throw new AssertionError("Identity/order at "+i);}
 static long structure(Set<?> set)throws Exception {
  long bytes=instrumentation.getObjectSize(set);
  if(set instanceof ObjectLinkedOpenHashSet<?>) {
   for(String name:List.of("key","link")){var f=ObjectLinkedOpenHashSet.class.getDeclaredField(name);f.setAccessible(true);bytes+=instrumentation.getObjectSize(f.get(set));}
  } else {
   var f=HashSet.class.getDeclaredField("map");f.setAccessible(true);Object map=f.get(set);bytes+=instrumentation.getObjectSize(map);
   f=HashMap.class.getDeclaredField("table");f.setAccessible(true);Object[] table=(Object[])f.get(map);
   if(table!=null){bytes+=instrumentation.getObjectSize(table);f=Class.forName("java.util.HashMap$Node").getDeclaredField("next");f.setAccessible(true);for(Object node:table)while(node!=null){bytes+=instrumentation.getObjectSize(node);node=f.get(node);}}
  }
  return bytes;
 }
}
'@ | Set-Content -Encoding UTF8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if ($LASTEXITCODE) {throw 'Compile failed'}
"Premain-Class: Check`n`n" | Set-Content -Encoding ascii "$output/manifest.mf"
& "$Jdk/bin/jar.exe" cfm "$output/agent.jar" "$output/manifest.mf" -C $output Check.class
if ($LASTEXITCODE) {throw 'Agent build failed'}
$sizes = 'E:/tmp/heap-analysis-20261117-third/scan-sizes.tsv'
[string[]]$extra = @()
if(Test-Path $sizes){$extra = @($sizes)}
foreach ($layout in @('-XX:+UseCompressedOops','-XX:-UseCompressedOops')) {
 & "$Jdk/bin/java.exe" -Xmx2g $layout --add-opens=java.base/java.util=ALL-UNNAMED "-javaagent:$output/agent.jar" -Xverify:all -cp "$output;$cp" Check @extra
 if ($LASTEXITCODE) {throw 'Scan check failed'}
}
