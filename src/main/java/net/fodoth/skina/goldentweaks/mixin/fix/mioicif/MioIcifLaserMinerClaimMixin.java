package net.fodoth.skina.goldentweaks.mixin.fix.mioicif;

import com.miophas.singularity_iteration.common.item.tools.mio_icif_laser_bullet;
import net.fodoth.skina.goldentweaks.compat.ftbchunks.GTClaimProtection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 采矿镭射枪的弹体在命中方块时直接调用 {@code level.destroyBlock}，不会经过原版破坏流程。
 * 虽然它自己会发布一次 {@code BreakEvent}，这里再按 FTB Chunks 的领地规则直接拦一次，
 * 保证命中他人领地内的方块时既不破坏、也不引爆破模式。
 */
@Mixin(value = mio_icif_laser_bullet.class, remap = false)
public abstract class MioIcifLaserMinerClaimMixin {

    @Inject(method = "onHitBlock", at = @At("HEAD"), cancellable = true)
    private void gt$preventClaimBlockBreak(BlockHitResult hit, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }

        Entity owner = ((mio_icif_laser_bullet) (Object) this).getOwner();
        if (GTClaimProtection.isBlockBreakPrevented(owner, hit.getBlockPos())) {
            self.discard();
            ci.cancel();
        }
    }
}
