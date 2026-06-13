package net.fodoth.skina.goldentweaks.mixin.fix.traveloptics;

import com.gametechbc.traveloptics.api.utils.SummonCheckHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SummonCheckHelper.class)
public abstract class SummonCheckHelperMixin {

    /**
     * 修复 IMagicSummon.getSummoner() 返回 Entity 类型的问题
     */
    @WrapOperation(
            method = "lambda$hasActiveSummons$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/entity/mobs/IMagicSummon;getSummoner()Lnet/minecraft/world/entity/LivingEntity;"
            )
    )
    private static LivingEntity fixGetSummonerReturnType(IMagicSummon instance, Operation<LivingEntity> original) {
        Entity summoner = instance.getSummoner();
        if (summoner instanceof LivingEntity livingSummoner) {
            return livingSummoner;
        }
        return null;
    }
}