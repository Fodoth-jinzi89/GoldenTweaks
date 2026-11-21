package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.miophas.singularity_iteration.common.blockentity.producer.mio_icif_tesla;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * 特斯拉线圈只电击敌对生物：攻击目标搜索完成后过滤掉非 {@link Enemy} 的活体，
 * 玩家、动物、村民与宠物不再被电击。以上行为受配置项控制，默认开启。
 */
@Mixin(value = mio_icif_tesla.class, remap = false)
public abstract class MioIcifTeslaCoilHostileOnlyMixin {

    @WrapOperation(
            method = "performAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
            )
    )
    private List<LivingEntity> gt$onlyAttackHostileMobs(
            Level level, Class<LivingEntity> entityClass, AABB area,
            Operation<List<LivingEntity>> original) {

        List<LivingEntity> entities = original.call(level, entityClass, area);

        if (!GoldenTweaksCommonConfig.isMioIcifTeslaCoilHostileOnly()) {
            return entities;
        }

        return entities.stream().filter(entity -> entity instanceof Enemy).toList();
    }
}
