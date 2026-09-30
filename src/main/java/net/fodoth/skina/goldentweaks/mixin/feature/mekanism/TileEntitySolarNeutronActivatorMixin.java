package net.fodoth.skina.goldentweaks.mixin.feature.mekanism;

import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import mekanism.common.util.WorldUtils;
import net.fodoth.skina.goldentweaks.compat.mekanism.GTMekanismCompactSpace;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntitySolarNeutronActivator.class, remap = false)
public abstract class TileEntitySolarNeutronActivatorMixin {

    @Inject(method = "canSeeSun", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceCanSeeSun(CallbackInfoReturnable<Boolean> cir) {
        if (GTMekanismCompactSpace.isCompact(((BlockEntity) (Object) this).getLevel())) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(
            method = "recalculateProductionRate",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/common/util/WorldUtils;getSunBrightness(Lnet/minecraft/world/level/Level;F)F"
            )
    )
    private float goldentweaks$compactSpaceFullBrightness(Level level, float partialTicks) {
        return GTMekanismCompactSpace.isCompact(level) ? 1.0F : WorldUtils.getSunBrightness(level, partialTicks);
    }
}
