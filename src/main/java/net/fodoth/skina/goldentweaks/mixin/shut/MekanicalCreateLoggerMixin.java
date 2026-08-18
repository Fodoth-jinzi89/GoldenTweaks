package net.fodoth.skina.goldentweaks.mixin.shut;

import io.github.langqi99.mekanicalcreate.content.CreateFamilyRecipeDiscovery;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CreateFamilyRecipeDiscovery.class, remap = false)
public class MekanicalCreateLoggerMixin {

    @Redirect(
            method = "referencedProcessingRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
            )
    )
    private static void suppressUnsafeInspectionWarning(Logger logger, String message,
                                                        Object blockId, Object error) {
    }
}
