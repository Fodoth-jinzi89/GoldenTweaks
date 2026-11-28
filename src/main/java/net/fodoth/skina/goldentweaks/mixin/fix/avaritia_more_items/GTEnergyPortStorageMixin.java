package net.fodoth.skina.goldentweaks.mixin.fix.avaritia_more_items;

import net.fodoth.skina.goldentweaks.content.multiblock.common.GTEnergyBufferFix;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.EnergyPortStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restores a port's stored energy losslessly.
 *
 * <p>AMI's {@code EnergyPortStorage.load} calls {@code receiveEnergy(stored, false)}, which is
 * capped at {@code maxReceive}. Its constructor sets {@code maxReceive} equal to the capacity, but
 * the buffers are built with a per-tick intake, so a port holding more than one tick's worth loses
 * the rest on load and looks reset after re-entering the world.</p>
 *
 * <p>The stored figure is written straight into the buffer here instead. The tag remains AMI's own
 * {@code Energy} key, so saves stay compatible in both directions.</p>
 */
@Mixin(value = EnergyPortStorage.class, remap = false)
public class GTEnergyPortStorageMixin {

    @Shadow
    private EnergyStorage energy;

    @Inject(method = "load", at = @At("HEAD"), cancellable = true, remap = false)
    private void goldentweaks$restoreWithoutLoss(CompoundTag tag, HolderLookup.Provider registries,
                                                 CallbackInfo ci) {
        if (!tag.contains("Energy")) {
            return;
        }
        int stored = tag.getInt("Energy");
        if (GTEnergyBufferFix.restore(this.energy, stored)) {
            // Handled in full, so AMI's capped path is skipped entirely.
            ci.cancel();
        }
        // Otherwise fall through to AMI's own load, which is lossy but still better than nothing.
    }
}
