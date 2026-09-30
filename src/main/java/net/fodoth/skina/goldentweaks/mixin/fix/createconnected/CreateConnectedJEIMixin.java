package net.fodoth.skina.goldentweaks.mixin.fix.createconnected;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create: Connected refreshes its feature-toggle dependent items by adding/removing them from the recipe viewer at
 * runtime ({@code MANAGER.removeIngredientsAtRuntime} then {@code addIngredientsAtRuntime}).
 * <p>
 * Toomanyrecipeviewers backs the JEI runtime API with EMI, and by the time the server config sync arrives its
 * ingredient manager is already locked, so both calls throw {@code IllegalStateException} ("Tried to remove/add
 * ingredients after registry is locked"). The refresh aborts on the very first call, so it never actually hides
 * anything - it only floods the log. Skipping it while TMRV is installed removes the conflict without changing the
 * visible item list.
 */
@Mixin(targets = "com.hlysine.create_connected.compat.CreateConnectedJEI", remap = false)
public abstract class CreateConnectedJEIMixin {

    @Inject(method = "refreshItemList", at = @At("HEAD"), cancellable = true)
    private static void goldentweaks$skipLockedIngredientRefresh(CallbackInfo ci) {
        ci.cancel();
    }
}
