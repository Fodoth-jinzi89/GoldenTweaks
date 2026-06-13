package net.fodoth.skina.goldentweaks.mixin.fix.traveloptics;


import com.gametechbc.traveloptics.entity.summons.SummonedKoboleton;
import com.gametechbc.traveloptics.entity.summons.SummonedWadjet;
import com.gametechbc.traveloptics.init.TOEffects;
import com.gametechbc.traveloptics.spells.holy.SummonDesertDwellers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;


import javax.annotation.Nullable;
import java.util.Objects;

@Mixin(SummonDesertDwellers.class)
public abstract class SummonDesertDwellersMixin {

    /**
     * @reason 修复 SANDSTORM 粒子不存在的问题，直接移除粒子生成
     * @author Goldentweaks
     */
    @Overwrite
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable io.redspace.ironsspellbooks.api.magic.MagicData playerMagicData) {
        // 完全不生成粒子
    }

    /**
     * @reason 修复 SANDSTORM 粒子不存在的问题，直接移除所有粒子生成
     * @author Goldentweaks
     */
    @Overwrite
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       io.redspace.ironsspellbooks.api.spells.CastSource castSource,
                       io.redspace.ironsspellbooks.api.magic.MagicData playerMagicData) {
        // 只保留 summon 逻辑，移除所有粒子生成
        int summonTime = 12000;
        int koboletonCount = (int) this.getKoboletonCount(spellLevel);
        double radius = 3.5D;
        double angleIncrement = (Math.PI * 2D) / (double) koboletonCount;

        for (int i = 0; i < koboletonCount; ++i) {
            double angle = (double) i * angleIncrement;
            double xOffset = radius * Math.cos(angle);
            double zOffset = radius * Math.sin(angle);
            SummonedKoboleton koboleton = new SummonedKoboleton(world, entity);
            koboleton.setPos(entity.getX() + xOffset, entity.getY(), entity.getZ() + zOffset);
            Objects.requireNonNull(koboleton.getAttributes().getInstance(Attributes.ATTACK_DAMAGE))
                    .setBaseValue(this.getKoboletonDamage(spellLevel, entity));
            Objects.requireNonNull(koboleton.getAttributes().getInstance(Attributes.MAX_HEALTH))
                    .setBaseValue(this.getKoboletonHealth(spellLevel));
            koboleton.setHealth(koboleton.getMaxHealth());
            this.equip(koboleton);
            world.addFreshEntity(koboleton);
            koboleton.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    com.gametechbc.traveloptics.init.TOEffects.DESERT_DWELLER_TIMER, summonTime, 0, false, false, false));
            // 移除了粒子生成
        }

        SummonedWadjet wadjet = new SummonedWadjet(world, entity);
        wadjet.setPos(entity.position());
        Objects.requireNonNull(wadjet.getAttributes().getInstance(Attributes.ATTACK_DAMAGE))
                .setBaseValue(this.getWadjetDamage(spellLevel, entity));
        Objects.requireNonNull(wadjet.getAttributes().getInstance(Attributes.MAX_HEALTH))
                .setBaseValue(this.getWadjetHealth(spellLevel));
        wadjet.setHealth(wadjet.getMaxHealth());
        world.addFreshEntity(wadjet);
        wadjet.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                com.gametechbc.traveloptics.init.TOEffects.DESERT_DWELLER_TIMER, summonTime, 0, false, false, false));
        // 移除了粒子生成

        int effectAmplifier = 0;
        if (entity.hasEffect(com.gametechbc.traveloptics.init.TOEffects.DESERT_DWELLER_TIMER)) {
            effectAmplifier += Objects.requireNonNull(entity.getEffect(TOEffects.DESERT_DWELLER_TIMER)).getAmplifier() + 1;
        }

        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                com.gametechbc.traveloptics.init.TOEffects.DESERT_DWELLER_TIMER, summonTime, effectAmplifier, false, false, true));
        io.redspace.ironsspellbooks.api.util.CameraShakeManager.addCameraShake(
                new io.redspace.ironsspellbooks.api.util.CameraShakeData(entity.level(), 35, entity.position(), 25.0F));
    }

    // Shadow 方法
    @org.spongepowered.asm.mixin.Shadow
    protected abstract float getKoboletonCount(int spellLevel);

    @Shadow
    protected abstract float getKoboletonDamage(int spellLevel, LivingEntity caster);

    @Shadow
    protected abstract float getKoboletonHealth(int spellLevel);

    @Shadow
    protected abstract void equip(Mob mob);

    @Shadow
    protected abstract float getWadjetDamage(int spellLevel, LivingEntity caster);

    @Shadow
    protected abstract float getWadjetHealth(int spellLevel);
}