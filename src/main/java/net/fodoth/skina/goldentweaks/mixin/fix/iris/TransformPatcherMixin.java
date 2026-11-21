package net.fodoth.skina.goldentweaks.mixin.fix.iris;

import net.fodoth.skina.goldentweaks.compat.iris.GTGlslCaches;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TransformPatcher.class, remap = false)
public abstract class TransformPatcherMixin {
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void gt$configureCaches(CallbackInfo ci) {
        GTGlslCaches.configureOwner(TransformPatcher.class);
    }
}
