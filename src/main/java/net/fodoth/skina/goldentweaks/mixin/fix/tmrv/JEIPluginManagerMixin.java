package net.fodoth.skina.goldentweaks.mixin.fix.tmrv;

import dev.nolij.toomanyrecipeviewers.plugin.DispatchStrategy;
import dev.nolij.toomanyrecipeviewers.plugin.JEIPluginManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = JEIPluginManager.class, remap = false)
public class JEIPluginManagerMixin {

    @ModifyArg(
            method = "registerIngredients",
            at = @At(value = "INVOKE", target = "Ldev/nolij/toomanyrecipeviewers/plugin/JEIPluginManager;dispatch(Ljava/util/function/Consumer;Ldev/nolij/toomanyrecipeviewers/plugin/DispatchStrategy;ZZJ)V"),
            index = 1)
    private DispatchStrategy goldentweaks$registerIngredientsSynchronously(DispatchStrategy original) {
        return DispatchStrategy.SYNC_EMI;
    }
}
