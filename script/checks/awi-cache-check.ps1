param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21', [string]$AwiJar = '')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/awi-cache-check'
New-Item -ItemType Directory -Force $output | Out-Null
$sources = @{
    'dev/emi/emi/api/recipe/EmiRecipeCategory.java' = 'package dev.emi.emi.api.recipe; public class EmiRecipeCategory {}'
    'dev/emi/emi/registry/EmiRecipes.java' = 'package dev.emi.emi.registry; public class EmiRecipes { public static java.util.List<dev.emi.emi.api.recipe.EmiRecipeCategory> categories = new java.util.ArrayList<>(); }'
    'com/yanny/awi/emi/compatibility/EmiCompatibility.java' = 'package com.yanny.awi.emi.compatibility; public class EmiCompatibility { public static class RecipeCategory extends dev.emi.emi.api.recipe.EmiRecipeCategory {} }'
    'org/spongepowered/asm/mixin/injection/callback/CallbackInfo.java' = 'package org.spongepowered.asm.mixin.injection.callback; public class CallbackInfo { public boolean cancelled; public void cancel() { cancelled=true; } }'
    'Check.java' = @'
import java.util.*;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fodoth.skina.goldentweaks.mixin.fix.awi.JeiCompatibilityMixin;
public class Check extends JeiCompatibilityMixin {
    public static void main(String[] args) throws Exception {
        var hook=JeiCompatibilityMixin.class.getDeclaredMethod("gt$preferNativeEmi",CallbackInfo.class);hook.setAccessible(true);
        var field=JeiCompatibilityMixin.class.getDeclaredField("dimensions");field.setAccessible(true);
        Map<Object,Object> map=new HashMap<>();Object data=new Object();map.put("old",data);field.set(null,map);
        Check instance=new Check();CallbackInfo absent=new CallbackInfo();hook.invoke(instance,absent);
        if(absent.cancelled || map.get("old")!=data)throw new AssertionError("Missing native plugin must fall back");
        EmiRecipes.categories.add(new EmiRecipeCategory());CallbackInfo unrelated=new CallbackInfo();hook.invoke(instance,unrelated);
        if(unrelated.cancelled || map.size()!=1)throw new AssertionError("Unrelated category must not disable JEI");
        EmiRecipes.categories.add(new com.yanny.awi.emi.compatibility.EmiCompatibility.RecipeCategory());
        CallbackInfo ready=new CallbackInfo();hook.invoke(instance,ready);
        if(!ready.cancelled || !map.isEmpty() || EmiRecipes.categories.size()!=2)throw new AssertionError("Duplicate retained or native data changed");
        map.put("reload",data);CallbackInfo reload=new CallbackInfo();hook.invoke(instance,reload);
        if(!reload.cancelled || !map.isEmpty())throw new AssertionError("Reload retained staging data");
        EmiRecipes.categories.clear();CallbackInfo fallback=new CallbackInfo();hook.invoke(instance,fallback);
        if(fallback.cancelled)throw new AssertionError("Fallback lost after native data cleared");
        System.out.println("PASS: compiled hook preserves fallback/native categories; duplicate staging cleared; reload verified");
    }
}
'@
}
$files = foreach ($entry in $sources.GetEnumerator()) {
    $file = Join-Path $output $entry.Key
    New-Item -ItemType Directory -Force (Split-Path $file -Parent) | Out-Null
    Set-Content -LiteralPath $file -Value $entry.Value -Encoding utf8
    $file
}
$classpath = Join-Path $root 'build/classes/java/main'
& (Join-Path $Jdk 'bin/javac.exe') -encoding UTF-8 -cp $classpath -d $output @files
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
& (Join-Path $Jdk 'bin/java.exe') -Xverify:all -cp "$output;$classpath" Check
if ($LASTEXITCODE -ne 0) { throw 'AWI hook check failed' }
if ($AwiJar) {
    $code = (& (Join-Path $Jdk 'bin/javap.exe') -c -p -s -classpath $AwiJar com.yanny.awi.jei.compatibility.JeiCompatibility) -join "`n"
    if ($LASTEXITCODE -ne 0 -or -not $code.Contains('descriptor: (Lmezz/jei/api/registration/IRecipeCategoryRegistration;)V') -or
            -not $code.Contains('dimensions:Ljava/util/Map;') -or -not $code.Contains('GenericUtils.register:')) {
        throw 'AWI registration structure changed'
    }
    $native = (& (Join-Path $Jdk 'bin/javap.exe') -p -classpath $AwiJar 'com.yanny.awi.emi.compatibility.EmiCompatibility$RecipeCategory') -join "`n"
    if ($LASTEXITCODE -ne 0 -or -not $native.Contains('extends dev.emi.emi.api.recipe.EmiRecipeCategory')) { throw 'Native category changed' }
    Write-Output 'PASS: upstream JEI staging registration and native EMI category verified'
}
