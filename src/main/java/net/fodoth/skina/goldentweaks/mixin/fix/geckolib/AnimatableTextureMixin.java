package net.fodoth.skina.goldentweaks.mixin.fix.geckolib;

import net.fodoth.skina.goldentweaks.mixin.fix.geckolib.accessor.SimpleTextureAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.cache.texture.AnimatableTexture;

@Mixin(value = AnimatableTexture.class, remap = false)
public abstract class AnimatableTextureMixin {

    @Inject(
            method = "load",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$validateTexture(
            ResourceManager manager,
            CallbackInfo ci
    ) {
        ResourceLocation location =
                ((SimpleTextureAccessor) this).goldentweaks$getLocation();

        if (manager.getResource(location).isEmpty()) {
            ci.cancel();
        }
    }
}