# Run after ./gradlew.bat build. Executes the compiled hook with isolated API doubles.
param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/apothic-enchanting-check'
New-Item -ItemType Directory -Force -Path $output | Out-Null
$sources = @{
    'net/minecraft/core/Holder.java' = 'package net.minecraft.core; public interface Holder<T> {}'
    'net/minecraft/world/item/enchantment/Enchantment.java' = 'package net.minecraft.world.item.enchantment; public class Enchantment {}'
    'dev/shadowsoffire/apothic_enchanting/ApothicEnchanting.java' = @'
package dev.shadowsoffire.apothic_enchanting;
public class ApothicEnchanting {
    public static final java.util.Map<Object, Object> ENCHANTMENT_INFO = new java.util.HashMap<>();
}
'@
    'dev/shadowsoffire/apothic_enchanting/EnchantmentInfo.java' = @'
package dev.shadowsoffire.apothic_enchanting;
import net.minecraft.core.Holder;
public class EnchantmentInfo {
    public static Holder<?> lastHolder;
    public static final EnchantmentInfo FALLBACK = new EnchantmentInfo();
    public static EnchantmentInfo fallback(Holder<?> holder) { lastHolder = holder; return FALLBACK; }
}
'@
    'org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable.java' = @'
package org.spongepowered.asm.mixin.injection.callback;
public class CallbackInfoReturnable<T> {
    public boolean cancelled;
    public T value;
    public void setReturnValue(T value) { this.value = value; cancelled = true; }
}
'@
    'Check.java' = @'
import dev.shadowsoffire.apothic_enchanting.*;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
public class Check {
    public static void main(String[] args) throws Exception {
        var type = Class.forName("net.fodoth.skina.goldentweaks.mixin.fix.apothic_enchanting.ApothicEnchantingMixin");
        var hook = type.getDeclaredMethod("gt$fallbackBeforeLoaded", Holder.class, CallbackInfoReturnable.class);
        hook.setAccessible(true);
        Holder<Enchantment> holder = new Holder<>() {};
        var early = new CallbackInfoReturnable<EnchantmentInfo>();
        hook.invoke(null, holder, early);
        if (!early.cancelled || early.value != EnchantmentInfo.FALLBACK || EnchantmentInfo.lastHolder != holder)
            throw new AssertionError("Empty information must return upstream fallback for the requested holder");
        if (!ApothicEnchanting.ENCHANTMENT_INFO.isEmpty()) throw new AssertionError("Fallback must not be cached");
        var configured = new EnchantmentInfo();
        ApothicEnchanting.ENCHANTMENT_INFO.put(holder, configured);
        var loaded = new CallbackInfoReturnable<EnchantmentInfo>();
        hook.invoke(null, holder, loaded);
        if (loaded.cancelled || ApothicEnchanting.ENCHANTMENT_INFO.get(holder) != configured)
            throw new AssertionError("Loaded information must pass through unchanged");
        ApothicEnchanting.ENCHANTMENT_INFO.clear();
        var restart = new CallbackInfoReturnable<EnchantmentInfo>();
        hook.invoke(null, holder, restart);
        if (!restart.cancelled) throw new AssertionError("Fallback must work again after server data is cleared");
        System.out.println("PASS: empty, loaded and cleared information; no fallback cache");
    }
}
'@
}
$files = foreach ($entry in $sources.GetEnumerator()) {
    $file = Join-Path $output $entry.Key
    New-Item -ItemType Directory -Force -Path (Split-Path $file -Parent) | Out-Null
    Set-Content -LiteralPath $file -Value $entry.Value -Encoding UTF8
    $file
}
& (Join-Path $Jdk 'bin/javac.exe') -encoding UTF-8 -d $output @files
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
& (Join-Path $Jdk 'bin/java.exe') -cp "$output;$(Join-Path $root 'build/classes/java/main')" Check
if ($LASTEXITCODE -ne 0) { throw 'Hook regression check failed' }
