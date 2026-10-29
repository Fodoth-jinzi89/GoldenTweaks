package net.fodoth.skina.goldentweaks.mixin.fix.cataclysm;

import org.spongepowered.asm.mixin.Mixin;

/**
 * 空 mixin：唯一作用是让 mixin 插件对灾变 {@code ModParticle} 调用 preApply，
 * 由 {@code CataclysmParticleCompatASM} 补回 traveloptics 编译期引用、但灾变新版已移除的
 * {@code SANDSTORM} 粒子字段。用 {@code targets} 字符串，避免本类链接灾变的类。
 */
@Mixin(targets = "com.github.L_Ender.cataclysm.init.ModParticle", remap = false)
public abstract class ModParticleSandstormCompatMixin {
}
