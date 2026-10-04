package net.fodoth.skina.goldentweaks.mixin.fix.appflux;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.implementations.PatternProviderScreen;
import net.fodoth.skina.goldentweaks.mixin.fix.appflux.accessor.WidgetContainerMergeAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes ae2helpers' separately drawn "importupgrades" panel; its slot now lives in the AppliedFlux / extendedae_plus
 * upgrade sidebar (see {@link PatternProviderMenuMergeMixin}).
 */
@Mixin(AEBaseScreen.class)
public abstract class PatternProviderScreenMergeMixin {

    @Shadow
    @Final
    protected WidgetContainer widgets;

    @Inject(method = "init", at = @At("HEAD"))
    private void gt$dropImportUpgradesPanel(CallbackInfo ci) {
        if (!((Object) this instanceof PatternProviderScreen<?>)) {
            return;
        }

        var compositeWidgets = ((WidgetContainerMergeAccessor) (Object) this.widgets).gt$getCompositeWidgets();
        if (compositeWidgets.containsKey("importupgrades")) {
            compositeWidgets.remove("importupgrades");
        }
    }
}
