param([string]$Jdk='C:/Program Files/BellSoft/LibericaJDK-21')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$output=Join-Path $root 'build/model-texture-names-check'
New-Item -ItemType Directory -Force $output | Out-Null
$cp=(Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()
@'
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;
import java.lang.reflect.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import com.google.gson.*;
import net.minecraft.client.renderer.block.model.*;
import net.fodoth.skina.goldentweaks.util.GTModelTextureNames;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
public class Check {
 static class Loader extends ClassLoader {Class<?> define(byte[] data){return defineClass(null,data,0,data.length);}}
 public static void main(String[] args)throws Exception {
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);
  var fixture=new ClassNode();fixture.version=Opcodes.V21;fixture.access=Opcodes.ACC_PUBLIC;fixture.name="FaceNameHook";fixture.superName="java/lang/Object";
  var hook=node.methods.stream().filter(m->m.name.equals("gt$shareTextureName")).findFirst().orElseThrow();hook.access=Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC;hook.name="apply";
  for(var insn:hook.instructions)if(insn instanceof VarInsnNode v)v.var--;
  hook.localVariables=null;hook.visibleAnnotations=null;hook.invisibleAnnotations=null;fixture.methods.add(hook);
  var writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);fixture.accept(writer);
  Method apply=new Loader().define(writer.toByteArray()).getMethod("apply",CallbackInfoReturnable.class);
  Class<?> parser=BlockElementFace.Deserializer.class;var ctor=parser.getDeclaredConstructor();ctor.setAccessible(true);Object instance=ctor.newInstance();
  var parse=parser.getDeclaredMethod("getTexture",JsonObject.class);parse.setAccessible(true);
  List<String> names=List.of("#0","#side","namespace:block/texture","#中文","","#Side");
  var retained=new HashMap<String,String>();
  for(int i=0;i<100000;i++) {
   String text=names.get(i%names.size());var json=new JsonObject();json.addProperty("texture",new String(text));
   String result=(String)parse.invoke(instance,json);var callback=new CallbackInfoReturnable<String>("getTexture",true,result);apply.invoke(null,callback);String shared=callback.getReturnValue();
   if(!shared.equals(text))throw new AssertionError("Text changed");String previous=retained.putIfAbsent(text,shared);if(previous!=null && previous!=shared)throw new AssertionError("Not shared");
  }
  if(retained.get("#side")==retained.get("#Side"))throw new AssertionError("Case folded");
  try{parse.invoke(instance,new JsonObject());throw new AssertionError("Missing field accepted");}catch(InvocationTargetException expected){if(!(expected.getCause() instanceof JsonParseException))throw expected;}
  String expected=retained.get("#side");
  try(var pool=Executors.newFixedThreadPool(8)) {
   List<Future<?>> jobs=new ArrayList<>();for(int i=0;i<8;i++)jobs.add(pool.submit(()->{for(int j=0;j<10000;j++)if(GTModelTextureNames.share(new String("#side"))!=expected)throw new AssertionError("Concurrent identity");}));for(var job:jobs)job.get();
  }
  // Real model parsing preserves independent mutable face parent/UV objects.
  String json="{\"elements\":[{\"from\":[0,0,0],\"to\":[16,16,16],\"faces\":{\"north\":{\"texture\":\"#side\"}}}]}";
  var a=BlockModel.fromString(json).getElements().getFirst();var b=BlockModel.fromString(json).getElements().getFirst();
  var fa=a.faces.values().iterator().next();var fb=b.faces.values().iterator().next();
  if(fa==fb || fa.uv()==fb.uv() || fa.parent()==fb.parent() || fa.parent().getValue()!=a || fb.parent().getValue()!=b)throw new AssertionError("Mutable face state shared");
  System.out.println("PASS: compiled return hook and real texture parser 100000 cases; exact text/case/Unicode/empty, missing-field rejection, 8 threads; real model faces/UV/parents independent");
 }
}
'@ | Set-Content -Encoding utf8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if($LASTEXITCODE){throw 'Compile failed'}
& "$Jdk/bin/java.exe" -Xmx2g -Xverify:all -cp "$output;$cp" Check "$root/build/classes/java/main/net/fodoth/skina/goldentweaks/mixin/fix/model/BlockElementFaceDeserializerMixin.class"
if($LASTEXITCODE){throw 'Model face text check failed'}
