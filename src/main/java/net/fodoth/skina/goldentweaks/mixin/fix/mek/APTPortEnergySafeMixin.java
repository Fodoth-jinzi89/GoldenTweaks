package net.fodoth.skina.goldentweaks.mixin.fix.mek;

import fr.iglee42.evolvedmekanism.multiblock.apt.TileEntityAPTPort;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 延迟 multiblock energy 访问（防 load 阶段崩溃）
 */
@Mixin(TileEntityAPTPort.class)
public abstract class APTPortEnergySafeMixin {

    @Inject(
            method = "getInitialEnergyContainers",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fixEnergy(IContentsListener listener,
                           CallbackInfoReturnable<IEnergyContainerHolder> cir) {

        TileEntityAPTPort self = (TileEntityAPTPort)(Object)this;

        cir.setReturnValue(side -> {

            if (self.getLevel() == null || self.getMultiblock() == null) {
                cir.setReturnValue(side1 -> java.util.Collections.emptyList());
            }

            return self.getMultiblock()
                    .getEnergyContainers(side);
        });
    }
}