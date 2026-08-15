package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import thaumcraft.common.entities.ShockOrbEntity;

import java.util.function.Predicate;

/**
 * 震荡波伤害模式（移植自 TC4Tweaks 的 earthShockHarmMode）。
 *
 * <p>移植版 {@code onHit} 使用恒真的 Predicate（除施法者外一切实体都受伤）。
 * 这里把 {@code ServerLevel.getEntities} 的目标过滤替换为按配置选择的模式：
 * OnlyLiving / ExceptItemXp / AllEntity。</p>
 */
@Mixin(value = ShockOrbEntity.class, remap = false)
public abstract class ShockOrbEntityMixin {

    @ModifyArg(
            method = "onHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
            ),
            index = 2
    )
    private static Predicate<Entity> gt$earthShockTargetFilter(Predicate<Entity> original) {
        return switch (GoldenTweaksCommonConfig.getEarthShockHarmMode()) {
            case OnlyLiving -> entity -> entity instanceof LivingEntity;
            case ExceptItemXp -> entity -> !(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb);
            case AllEntity -> original;
        };
    }
}
