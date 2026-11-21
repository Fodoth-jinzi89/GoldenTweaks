param([string]$Jdk='C:/Program Files/BellSoft/LibericaJDK-21')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$output=Join-Path $root 'build/thaum-constructor-selector-check'
New-Item -ItemType Directory -Force $output | Out-Null
$cp=(Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()
@'
import java.util.*;
import java.util.jar.*;
import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.mixin.injection.selectors.ElementNode;
import org.spongepowered.asm.mixin.injection.selectors.ITargetSelector;
public class Check {
 public static void main(String[] args)throws Exception {
  var mixin=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(mixin,0);
  var hook=mixin.methods.stream().filter(m->m.name.equals("gt$lazyStyles")).findFirst().orElseThrow();
  AnnotationNode redirect=hook.visibleAnnotations.stream().filter(a->a.desc.endsWith("/Redirect;")).findFirst().orElseThrow();
  List<String> selectors=null;String target=null;
  for(int i=0;i<redirect.values.size();i+=2) {
   String key=(String)redirect.values.get(i);Object value=redirect.values.get(i+1);
   if(key.equals("method"))selectors=(List<String>)value;
   if(key.equals("at")){var at=(AnnotationNode)value;for(int j=0;j<at.values.size();j+=2)if(at.values.get(j).equals("target"))target=(String)at.values.get(j+1);}
  }
  if(selectors==null || selectors.size()!=1 || !selectors.getFirst().equals("<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lnet/minecraft/client/model/EntityModel;)V"))throw new AssertionError("Ambiguous constructor selector");
  try(var jar=new JarFile(args[1])) {
   var renderer=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry("thaumcraft/client/renderers/entity/LegacyThaumcraftMobRenderer.class"))).accept(renderer,0);
   var selector=MemberInfo.parse(selectors.getFirst(),null).configure(ITargetSelector.Configure.SELECT_MEMBER);
   var invocation=MemberInfo.parse(target,null);
   int methods=0,calls=0;
   for(var method:renderer.methods)if(selector.match(ElementNode.of(renderer,method)).isExactMatch()) {
    methods++;if((method.access&Opcodes.ACC_PRIVATE)==0)throw new AssertionError("Not private delegate");
    boolean initialized=false;
    for(var insn:method.instructions)if(insn instanceof MethodInsnNode call) {
     if(call.getOpcode()==Opcodes.INVOKESPECIAL && call.name.equals("<init>") && call.owner.equals(renderer.superName))initialized=true;
     if(invocation.match(ElementNode.of(call)).isExactMatch()){if(!initialized)throw new AssertionError("Redirect before super");calls++;}
    }
   }
   if(methods!=1 || calls!=1)throw new AssertionError("Target selection: "+methods+" constructors / "+calls+" calls");
   System.out.println("PASS: actual compiled @Redirect + runtime Mixin selector selects exactly one private constructor and one factory call after super");
  }
 }
}
'@ | Set-Content -Encoding utf8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if($LASTEXITCODE){throw 'Compile failed'}
$jar=Get-ChildItem -LiteralPath 'E:/机械动力魔法大冒险/.minecraft/versions/hkx/mods' -Filter 'thaumcraft-*.jar' | Select-Object -First 1
& "$Jdk/bin/java.exe" -Xverify:all -cp "$output;$cp" Check "$root/build/classes/java/main/net/fodoth/skina/goldentweaks/mixin/feature/thaumcraft/LegacyThaumcraftMobRendererMixin.class" $jar.FullName
if($LASTEXITCODE){throw 'Constructor selector check failed'}
