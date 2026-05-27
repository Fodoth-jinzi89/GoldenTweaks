package net.fodoth.skina.goldentweaks.mixin.shut;

import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TooManyRecipeViewersMod.class, remap = false)
public class TooManyRecipeViewersModMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void silence(CallbackInfo ci) {
        try {
            java.lang.reflect.Field f =
                    TooManyRecipeViewersMod.class.getDeclaredField("LOGGER");

            f.setAccessible(true);
            f.set(null, org.slf4j.helpers.NOPLogger.NOP_LOGGER);
        } catch (Exception ignored) {}
    }
}
