package net.fodoth.skina.goldentweaks.mixin.fix.colorwheel;

import net.fodoth.skina.goldentweaks.compat.iris.GTGlslCaches;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.djefrey.colorwheel.compile.transform.ClrwlTransformPatcher", remap = false)
public abstract class ClrwlTransformPatcherMixin {
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void gt$configureCaches(CallbackInfo ci) throws ClassNotFoundException {
        GTGlslCaches.configureTwoTierOwner(Class.forName("dev.djefrey.colorwheel.compile.transform.ClrwlTransformPatcher"));
    }
}
