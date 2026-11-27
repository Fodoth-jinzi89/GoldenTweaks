package net.fodoth.skina.goldentweaks.mixin.fix.jade;

import net.fodoth.skina.goldentweaks.compat.jade.GTPortPanels;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Stops Jade's own item, fluid and energy panels from being drawn for a general assembly.
 *
 * <p>Jade adds those automatically to anything with an inventory, fluid or energy capability, which
 * is exactly what the assembly registers so pipes and the machine can reach it. The result was
 * several panels on one block: ours, then a raw item list, then a raw fluid list — with the raw
 * ones unsorted and showing every cell, so they read as a duplicate rather than as more detail.</p>
 *
 * <p>Cancelling the tooltip alone is not enough. The server half collects the contents first and
 * ships them over, so the data would still be gathered and sent on every hover, for a panel nobody
 * draws. Both halves are therefore cancelled.</p>
 *
 * <p>The chemical panel is not covered here: it belongs to Mekanism, not to Jade. See
 * {@link GeneralPortMekanismPanelMixin}.</p>
 */
@Mixin(value = {
        snownee.jade.addon.universal.ItemStorageProvider.class,
        snownee.jade.addon.universal.FluidStorageProvider.class,
        snownee.jade.addon.universal.EnergyStorageProvider.class
}, remap = false)
public abstract class GeneralPortStoragePanelMixin {

    /** Skips the panel for an assembly. */
    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private void goldentweaks$skipGeneralTooltip(ITooltip tooltip, Accessor<?> accessor,
                                                 IPluginConfig config, CallbackInfo ci) {
        if (GTPortPanels.isAssembly(accessor)) {
            ci.cancel();
        }
    }

    /** Skips gathering the contents for that same panel. */
    @Inject(method = "appendServerData", at = @At("HEAD"), cancellable = true, remap = false)
    private void goldentweaks$skipGeneralData(CompoundTag tag, Accessor<?> accessor, CallbackInfo ci) {
        if (GTPortPanels.isAssembly(accessor)) {
            ci.cancel();
        }
    }
}
