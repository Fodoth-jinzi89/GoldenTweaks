param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/model-mesh-check'
New-Item -ItemType Directory -Force $output | Out-Null
$fastutil = Get-ChildItem "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/it.unimi.dsi/fastutil/8.5.12" -Recurse -Filter 'fastutil-8.5.12.jar' | Select-Object -First 1
$extra = Get-ChildItem "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/io.github.llamalad7/mixinextras-neoforge/0.5.3" -Recurse -Filter 'mixinextras-neoforge-0.5.3.jar' | Select-Object -First 1
$asm = foreach ($module in @('asm','asm-tree')) { Get-ChildItem "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/org.ow2.asm/$module" -Recurse -Filter "$module-9.8.jar" | Select-Object -First 1 -ExpandProperty FullName }
$cp = (@("$root/build/classes/java/main",$fastutil.FullName,$extra.FullName)+$asm)-join ';'
@"
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.util.*;
import java.nio.file.*;
public class Check {
 public static void main(String[] args) throws Exception {
  ClassNode exported=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(exported,0);
  net.fodoth.skina.goldentweaks.compat.acceleratedrendering.GTModelCacheASM.patch(exported);
  int small=0,defaults=0;
  for(MethodNode method:exported.methods)if(method.name.equals("<init>"))for(AbstractInsnNode insn:method.instructions)
   if(insn instanceof MethodInsnNode call && call.owner.equals("it/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap") && call.name.equals("<init>")){
    if(call.desc.equals("(I)V"))small++;if(call.desc.equals("()V"))defaults++;
   }
  if(small!=2 || defaults!=0)throw new AssertionError("Actual merged constructors not patched");
  net.fodoth.skina.goldentweaks.compat.acceleratedrendering.GTModelCacheASM.patch(exported);
  ClassNode fixture=new ClassNode();fixture.version=Opcodes.V21;fixture.access=Opcodes.ACC_PUBLIC;fixture.name="MeshFixture";fixture.superName="java/lang/Object";
  fixture.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"meshes","Ljava/util/Map;",null,null));
  fixture.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"merges","Ljava/util/Map;",null,null));
  MethodNode ctor=new MethodNode(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);
  ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"java/lang/Object","<init>","()V",false));
  for(String field:List.of("meshes","merges")) {
   for(MethodNode method:exported.methods)if(method.name.equals("<init>"))for(AbstractInsnNode insn:method.instructions)if(insn instanceof FieldInsnNode f && f.getOpcode()==Opcodes.PUTFIELD && f.name.equals(field)){
    var start=insn;for(int i=0;i<5;i++)start=start.getPrevious();
    var labels=new java.util.HashMap<LabelNode,LabelNode>();
    for(var n=start;n!=insn;n=n.getNext())ctor.instructions.add(n.clone(labels));
    ctor.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,"MeshFixture",field,"Ljava/util/Map;"));
   }
  }
  ctor.instructions.add(new InsnNode(Opcodes.RETURN));fixture.methods.add(ctor);
  ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);fixture.accept(writer);
  class Loader extends ClassLoader{Class<?> define(byte[] data){return defineClass(null,data,0,data.length);}}
  Class<?> type=new Loader().define(writer.toByteArray());Object instance=type.getConstructor().newInstance();
  var a=(Object2ObjectOpenHashMap<Object,Object>)type.getField("meshes").get(instance);
  var b=(Object2ObjectOpenHashMap<Object,Object>)type.getField("merges").get(instance);
  var keys=Object2ObjectOpenHashMap.class.getDeclaredField("key");keys.setAccessible(true);
  if(((Object[])keys.get(a)).length!=3 || !a.isEmpty() || a==b)throw new AssertionError("Initial shape/isolation");
  Map<Object,Object> reference=new HashMap<>();
  for(int i=0;i<10000;i++){a.put(i,i*7);reference.put(i,i*7);}
  a.put(null,42);reference.put(null,42);
  if(!a.equals(reference) || !b.isEmpty())throw new AssertionError("Growth/isolation");
  for(int i=0;i<5000;i++){a.remove(i);reference.remove(i);}
  a.computeIfAbsent("mesh",k->123);reference.computeIfAbsent("mesh",k->123);
  if(!a.equals(reference))throw new AssertionError("Map operations");
  a.clear();if(!a.isEmpty())throw new AssertionError("Clear");
  ClassNode node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);
  int sites=0;
  for(MethodNode m:node.methods)if(m.name.equals("<init>"))for(AbstractInsnNode n:m.instructions) {
   if(n instanceof MethodInsnNode call && call.owner.equals("it/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap") && call.name.equals("<init>") && call.desc.equals("()V"))sites++;
  }
  if(sites!=2)throw new AssertionError("Original exported sites changed: "+sites);
  for(String field:List.of("meshes","merges")) {
   if(node.fields.stream().noneMatch(f->f.name.equals(field)&&f.desc.equals("Ljava/util/Map;")))throw new AssertionError("Missing mesh field");
  }
  System.out.println("PASS: postApply patches both ACTUAL merged constructor sites; extracted patched initializer bytecode executes with 3-slot independent maps; growth/null/compute/remove/clear and idempotence verified");
 }
}
"@ | Set-Content "$output/Check.java" -Encoding utf8
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
& "$Jdk/bin/java.exe" -Xverify:all -cp "$output;$cp" Check 'E:\机械动力魔法大冒险\.minecraft\versions\hkx\.mixin.out\class\net\minecraft\client\resources\model\SimpleBakedModel.class'
if ($LASTEXITCODE -ne 0) { throw 'Mesh map check failed' }
