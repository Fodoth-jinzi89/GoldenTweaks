param([string]$Jdk='C:/Program Files/BellSoft/LibericaJDK-21')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$output=Join-Path $root 'build/golden-dandelion-check'
New-Item -ItemType Directory -Force $output | Out-Null
$cp=(Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()
$extras=Get-ChildItem 'C:/Users/peiranyu/.mcreator/gradle/caches/modules-2/files-2.1/io.github.llamalad7' -Recurse -Filter 'mixinextras-neoforge-*.jar' | Select-Object -First 1
$cp="$cp;$($extras.FullName)"
@'
import java.lang.reflect.*;
import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.AgeableMob;
import net.fodoth.skina.goldentweaks.mixin.feature.vanilla.AgeableMobMixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class Check extends AgeableMobMixin {
 static void require(boolean ok,String why) {if(!ok)throw new AssertionError(why);}
 static Method method(String name,Class<?>...types)throws Exception {
  Method m=AgeableMobMixin.class.getDeclaredMethod(name,types);m.setAccessible(true);return m;
 }
 public static void main(String[] args)throws Exception {
  var c=new Check();var hook=method("gt$holdBabyAge",AgeableMob.class,int.class,Operation.class);
  var save=method("gt$saveFrozenAge",CompoundTag.class,CallbackInfo.class);
  var load=method("gt$loadFrozenAge",CompoundTag.class,CallbackInfo.class);
  var clear=method("gt$clearAdultFreeze",int.class,CallbackInfo.class);
  int[] calls={0};Operation<Void> original=values->{calls[0]++;return null;};
  c.age=-1;c.gt$setAgeFrozen(true);
  for(int i=0;i<24000;i++)hook.invoke(c,null,0,original);
  require(calls[0]==0 && c.age==-1,"last baby tick must not grow to adult");
  var tag=new CompoundTag();save.invoke(c,tag,null);require(tag.getBoolean("goldentweaks:age_frozen"),"persist freeze");
  var restored=new Check();restored.age=-1;load.invoke(restored,tag,null);require(restored.gt$isAgeFrozen(),"restore baby freeze");
  c.gt$setAgeFrozen(false);hook.invoke(c,null,0,original);require(calls[0]==1,"unfrozen baby must age");
  save.invoke(c,tag,null);require(!tag.contains("goldentweaks:age_frozen"),"unfreeze removes flag");
  c.age=100;c.gt$setAgeFrozen(true);require(!c.gt$isAgeFrozen(),"cannot freeze adult cooldown");
  hook.invoke(c,null,99,original);require(calls[0]==2,"adult cooldown must run");
  restored.age=0;clear.invoke(restored,0,null);require(!restored.gt$isAgeFrozen(),"external adulthood clears flag");
  tag.putBoolean("goldentweaks:age_frozen",true);load.invoke(c,tag,null);require(!c.gt$isAgeFrozen(),"ignore stale adult flag");
  load.invoke(restored,new CompoundTag(),null);require(!restored.gt$isAgeFrozen(),"missing flag defaults false");
  var target=new ClassNode();try(var in=AgeableMob.class.getResourceAsStream("AgeableMob.class")){new ClassReader(in).accept(target,0);}
  var ai=target.methods.stream().filter(m->m.name.equals("aiStep")&&m.desc.equals("()V")).findFirst().orElseThrow();int sites=0;
  for(var insn:ai.instructions)if(insn instanceof MethodInsnNode call && call.owner.equals("net/minecraft/world/entity/AgeableMob")&&call.name.equals("setAge")&&call.desc.equals("(I)V"))sites++;
  require(sites==2,"upstream must have two natural aging calls");
  var mixin=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(mixin,0);
  var wrap=mixin.methods.stream().filter(m->m.name.equals("gt$holdBabyAge")).findFirst().orElseThrow();
  var annotation=wrap.visibleAnnotations.stream().filter(a->a.desc.endsWith("/WrapOperation;")).findFirst().orElseThrow();
  require(annotation.values.stream().anyMatch(v->v instanceof java.util.List<?> l&&l.contains("aiStep")),"compiled selector");
  System.out.println("PASS: compiled age hook, 24000 frozen ticks, last baby tick, unfreeze, adult cooldown, external adulthood, NBT roundtrip, stale/absent tags, two upstream sites");
 }
}
'@ | Set-Content -Encoding utf8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if($LASTEXITCODE){throw 'Check compile failed'}
& "$Jdk/bin/java.exe" -Xverify:all -cp "$output;$cp" Check "$root/build/classes/java/main/net/fodoth/skina/goldentweaks/mixin/feature/vanilla/AgeableMobMixin.class"
if($LASTEXITCODE){throw 'Age freeze check failed'}
