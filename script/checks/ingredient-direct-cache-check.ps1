param([string]$Jdk='C:/Program Files/BellSoft/LibericaJDK-21')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$output=Join-Path $root 'build/ingredient-direct-cache-check'
New-Item -ItemType Directory -Force $output | Out-Null
$cp=(Get-Content "$root/build/continuity-runtime-classpath.txt" -Raw).Trim()
@'
import java.util.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.jar.*;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.fodoth.skina.goldentweaks.mixin.fix.modernfix.IngredientMixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class Check extends IngredientMixin {
 static Field values,cache,custom;static Method hook;
 static Check make(Ingredient.Value[] v)throws Exception{var c=new Check();values.set(c,v);return c;}
 static CallbackInfoReturnable<ItemStack[]> invoke(Check c)throws Exception{var ci=new CallbackInfoReturnable<ItemStack[]>("getItems",true);hook.invoke(c,ci);return ci;}
 public static void main(String[] args)throws Exception {
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
  values=IngredientMixin.class.getDeclaredField("values");cache=IngredientMixin.class.getDeclaredField("itemStacks");custom=IngredientMixin.class.getDeclaredField("customIngredient");
  for(var f:List.of(values,cache,custom))f.setAccessible(true);
  hook=IngredientMixin.class.getDeclaredMethod("gt$cacheDirectItem",CallbackInfoReturnable.class);hook.setAccessible(true);
  for(int i=0;i<20000;i++) {
   var stack=new ItemStack(i%2==0?Items.STONE:Items.DIAMOND,1+i%64);
   var v=new Ingredient.ItemValue(stack);var c=make(new Ingredient.Value[]{v});
   var result=invoke(c);if(!result.isCancelled() || result.getReturnValue().length!=1 || result.getReturnValue()[0]!=stack)throw new AssertionError("Wrong items");
   ItemStack[] array=result.getReturnValue();if(invoke(c).getReturnValue()!=array)throw new AssertionError("Not cached");
   var other=make(new Ingredient.Value[]{v});if(invoke(other).getReturnValue()==array)throw new AssertionError("Array shared across ingredients");
   ItemStack[] preexisting={new ItemStack(Items.GOLD_INGOT)};cache.set(c,preexisting);
   if(invoke(c).getReturnValue()!=preexisting)throw new AssertionError("Existing cache replaced");
  }
  for(var v:new Ingredient.Value[][]{new Ingredient.Value[0],{new Ingredient.TagValue(TagKey.create(Registries.ITEM,ResourceLocation.parse("c:ingots")))},{new Ingredient.ItemValue(new ItemStack(Items.STONE)),new Ingredient.ItemValue(new ItemStack(Items.DIAMOND))}})
   if(invoke(make(v)).isCancelled())throw new AssertionError("Fallback intercepted");
  var c=make(new Ingredient.Value[]{new Ingredient.ItemValue(new ItemStack(Items.STONE))});
  custom.set(c,Proxy.newProxyInstance(Check.class.getClassLoader(),new Class[]{ICustomIngredient.class},(p,m,a)->{throw new AssertionError("Custom ingredient called");}));
  if(invoke(c).isCancelled() || cache.get(c)!=null)throw new AssertionError("Custom intercepted");
  // Real upstream overwrite priority and single ItemValue path: no copies/transforms of its stack.
  try(var jar=new JarFile(args[0])) {
   var node=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry("org/embeddedt/modernfix/common/mixin/perf/faster_ingredients/IngredientMixin.class"))).accept(node,0);
   var compute=node.methods.stream().filter(m->m.name.equals("computeItemsArray")).findFirst().orElseThrow();
   long copyCalls=0,getItemsCalls=0;
   for(var insn:compute.instructions)if(insn instanceof MethodInsnNode call){if(call.name.equals("copy"))copyCalls++;if(call.owner.equals("net/minecraft/world/item/crafting/Ingredient$Value") && call.name.equals("getItems"))getItemsCalls++;}
   if(copyCalls!=0 || getItemsCalls!=1)throw new AssertionError("Upstream expansion changed");
   var overwrite=node.methods.stream().filter(m->m.name.equals("getItems")).findFirst().orElseThrow();
   if(overwrite.instructions.size()==0)throw new AssertionError("Missing upstream method");
  }
  System.out.println("PASS: compiled hook 20000 direct values, stack identity/count, repeated cache, cache preservation, array isolation; empty/tag/multi/custom fallbacks; actual ModernFix expansion inspected");
 }
}
'@ | Set-Content -Encoding utf8 "$output/Check.java"
& "$Jdk/bin/javac.exe" -proc:none -encoding UTF-8 -cp $cp -d $output "$output/Check.java"
if($LASTEXITCODE){throw 'Compile failed'}
$modernfix=Get-ChildItem -LiteralPath 'E:/机械动力魔法大冒险/.minecraft/versions/hkx/mods' -Filter '*modernfix*.jar' | Select-Object -First 1
& "$Jdk/bin/java.exe" -Xmx2g -Xverify:all -cp "$output;$cp" Check $modernfix.FullName
if($LASTEXITCODE){throw 'Ingredient check failed'}
