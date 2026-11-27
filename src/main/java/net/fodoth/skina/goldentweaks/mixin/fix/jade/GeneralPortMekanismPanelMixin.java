package net.fodoth.skina.goldentweaks.mixin.fix.jade;

import net.fodoth.skina.goldentweaks.compat.jade.GTPortPanels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Stops Mekanism's own Jade panel from being drawn for a general assembly.
 *
 * <p>Jade's built-in item, fluid and energy panels are cancelled by
 * {@link GeneralPortStoragePanelMixin}. The chemical one is not Jade's: Mekanism registers a
 * renderer against {@code Block.class} and a data provider against {@code BlockEntity.class}, so
 * every block that answers the chemical capability gets a panel. The assembly answers it so
 * hoppers can reach the part, which is why a chemical list appeared beside ours.</p>
 *
 * <p>In its own class rather than added to the other mixin: this one names Mekanism types, and a
 * mixin that does is only applied when that mod is present. Keeping them apart means the Jade-only
 * half still works in a pack without Mekanism.</p>
 */
@Mixin(value = mekanism.common.integration.lookingat.jade.JadeTooltipRenderer.class, remap = false)
public abstract class GeneralPortMekanismPanelMixin {

    /** Skips Mekanism's panel for an assembly. */
    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private void goldentweaks$skipGeneralTooltip(ITooltip tooltip, Accessor<?> accessor,
                                                 IPluginConfig config, CallbackInfo ci) {
        if (GTPortPanels.isAssembly(accessor)) {
            ci.cancel();
        }
    }
}
