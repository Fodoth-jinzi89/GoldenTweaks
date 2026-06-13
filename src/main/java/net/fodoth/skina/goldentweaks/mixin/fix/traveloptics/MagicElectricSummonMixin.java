package net.fodoth.skina.goldentweaks.mixin.fix.traveloptics;

import com.gametechbc.traveloptics.api.entity.mobs.MagicElectricSummon;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MagicElectricSummon.class)
public interface MagicElectricSummonMixin {
    /**
     * 修复 getSummoner() 返回类型不匹配的问题
     */
    @WrapOperation(
            method = "onRemovedHelper",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gametechbc/traveloptics/api/entity/mobs/MagicElectricSummon;getSummoner()Lnet/minecraft/world/entity/LivingEntity;"
            )
    )
    private LivingEntity fixGetSummonerReturnType(MagicElectricSummon instance, Operation<LivingEntity> original) {
        Entity summoner = instance.getSummoner();
        return summoner instanceof LivingEntity livingSummoner ? livingSummoner : null;
    }
}
