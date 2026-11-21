package net.fodoth.skina.goldentweaks.mixin.fix.touhoulittlemaid.client.resource;

import com.github.tartaricacid.touhoulittlemaid.client.resource.GeckoModelLoader;
import net.fodoth.skina.goldentweaks.compat.touhoulittlemaid.GTAnimationConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GeckoModelLoader.class, remap = false)
public abstract class GeckoModelLoaderMixin {

    @Inject(method = "reload", at = @At("HEAD"))
    private static void gt$clearNumericCaches(CallbackInfo ci) {
        GTAnimationConstants.clearNumericCaches();
    }
}
