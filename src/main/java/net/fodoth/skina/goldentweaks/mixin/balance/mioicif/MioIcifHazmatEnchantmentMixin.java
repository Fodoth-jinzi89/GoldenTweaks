package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.util.RadiationProtectionUtil;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTHazmatEnchantment;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让「防化」附魔享有与防化服完全相同的辐射免疫。
 *
 * <p>核反应堆、核爆落尘、辐射方块等所有辐射来源最终都汇到
 * {@code RadiationProtectionUtil.isRadiationProtected}，因此只需在这里补一条分支。</p>
 */
@Mixin(value = RadiationProtectionUtil.class, remap = false)
public abstract class MioIcifHazmatEnchantmentMixin {

    @Inject(method = "isRadiationProtected", at = @At("HEAD"), cancellable = true)
    private static void gt$hazmatEnchantmentProtects(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (GTHazmatEnchantment.protects(entity)) {
            cir.setReturnValue(true);
        }
    }
}
