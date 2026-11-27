package net.fodoth.skina.goldentweaks.mixin.fix.jade;

import net.fodoth.skina.goldentweaks.compat.jade.GTPortPanels;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;

/**
 * Stops Mekanism from gathering its chemical list for a general assembly.
 *
 * <p>The renderer half is cancelled by {@link GeneralPortMekanismPanelMixin}. This is the other
 * half and it is not optional: the provider runs on the server and ships the contents over, so
 * without it every hover would still collect and send a list nobody draws.</p>
 *
 * <p>Separate from the renderer mixin because the two targets do not share a method — the renderer
 * has {@code appendTooltip}, this one has {@code appendServerData}, and a single mixin naming both
 * would fail to find one of them.</p>
 */
@Mixin(value = mekanism.common.integration.lookingat.jade.JadeDataProvider.class, remap = false)
public abstract class GeneralPortMekanismDataMixin {

    /**
     * Skips the chemical list for an assembly.
     *
     * <p>{@code BlockAccessor} rather than a generic: this provider declares the concrete type, so
     * the descriptor says so.</p>
     */
    @Inject(method = "appendServerData", at = @At("HEAD"), cancellable = true, remap = false)
    private void goldentweaks$skipGeneralData(CompoundTag tag, BlockAccessor accessor,
                                              CallbackInfo ci) {
        if (GTPortPanels.isAssembly(accessor)) {
            ci.cancel();
        }
    }
}
