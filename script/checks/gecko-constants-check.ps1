param([string]$Jdk='C:/Program Files/BellSoft/LibericaJDK-21')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$output=Join-Path $root 'build/gecko-constants-check'
New-Item -ItemType Directory -Force $output | Out-Null
$gecko=Get-ChildItem -LiteralPath 'E:/机械动力魔法大冒险/.minecraft/versions/hkx/mods' -Filter '*geckolib*.jar' | Select-Object -First 1
$cp=(Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()+';'+$gecko.FullName
@'
import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import software.bernie.geckolib.loading.math.value.Constant;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.math.MathParser;
import net.fodoth.skina.goldentweaks.compat.geckolib.GTGeckoConstants;
public class Check {
 public static void main(String[] args)throws Exception {
  for(double value:new double[]{0,-0.0,1,-1,Math.PI,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,1e-300}) {
   Object shared=GTGeckoConstants.share(new Constant(value));
   for(int i=0;i<10000;i++) {
    Object next=GTGeckoConstants.share(new Constant(value));
    if(next!=shared || Double.doubleToRawLongBits(((Constant)next).get())!=Double.doubleToRawLongBits(value))throw new AssertionError("Numeric mismatch");
   }
  }
  if(GTGeckoConstants.share(new Constant(0))==GTGeckoConstants.share(new Constant(-0.0)))throw new AssertionError("Signed zero merged");
  for(long bits:new long[]{0x7ff8000000000001L,0x7ff8000000000012L}) {
   var nan=new Constant(Double.longBitsToDouble(bits));if(GTGeckoConstants.share(nan)!=nan)throw new AssertionError("NaN changed");
  }
  MathValue dynamic=()->42;if(GTGeckoConstants.share(dynamic)!=dynamic || GTGeckoConstants.share(null)!=null)throw new AssertionError("Fallback");
  for(String text:List.of("0","1","-0.0","1.25","2 + 3","math.sin(45)")) {
   MathValue original=MathParser.compileMolang(text);MathValue shared=(MathValue)GTGeckoConstants.share(original);
   if(Double.doubleToRawLongBits(original.get())!=Double.doubleToRawLongBits(shared.get()))throw new AssertionError("Evaluation changed");
  }
  var expected=GTGeckoConstants.share(new Constant(123.456));
  try(var pool=Executors.newFixedThreadPool(8)) {
   var jobs=new ArrayList<Future<?>>();for(int i=0;i<8;i++)jobs.add(pool.submit(()->{for(int j=0;j<10000;j++)if(GTGeckoConstants.share(new Constant(123.456))!=expected)throw new AssertionError("Concurrent sharing");}));
   for(var job:jobs)job.get();
  }
  try(var jar=new JarFile(args[0])) {
   for(String[] target:new String[][]{{"software/bernie/geckolib/loading/math/MathParser","parseJson","compileMolang","parseSymbols"},{"software/bernie/geckolib/loading/json/typeadapter/BakedAnimationsAdapter","lambda$buildKeyframeStack$0"}}) {
    var node=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry(target[0]+".class"))).accept(node,0);
    for(int i=1;i<target.length;i++){String name=target[i];var method=node.methods.stream().filter(m->m.name.equals(name)).findFirst().orElseThrow();if((method.access&Opcodes.ACC_STATIC)==0 || !method.desc.endsWith("Lsoftware/bernie/geckolib/loading/math/MathValue;"))throw new AssertionError("Hook descriptor changed");}
   }
  }
  System.out.println("PASS: real immutable Constant sharing 80000 cases, signed zero/NaN payload/dynamic fallback, real Molang evaluations, 8 threads, actual upstream hook descriptors");
 }
}
'@ | Set-Content -Encoding utf8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if($LASTEXITCODE){throw 'Compile failed'}
& "$Jdk/bin/java.exe" -Xmx2g -Xverify:all -cp "$output;$cp" Check $gecko.FullName
if($LASTEXITCODE){throw 'Gecko check failed'}
