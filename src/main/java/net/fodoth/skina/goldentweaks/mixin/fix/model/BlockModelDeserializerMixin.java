package net.fodoth.skina.goldentweaks.mixin.fix.model;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.model.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockModel$Deserializer")
public abstract class BlockModelDeserializerMixin {
    @Inject(method = "getTextureMap", at = @At("RETURN"), cancellable = true)
    private void gt$compactTextures(CallbackInfoReturnable<Map<String, Either<Material, String>>> cir) {
        Map<String, Either<Material, String>> textures = cir.getReturnValue();
        // The private parser has not exposed this map yet. Keep empty maps and custom replacements.
        if (textures.getClass() == HashMap.class && !textures.isEmpty()) {
            cir.setReturnValue(new Object2ObjectOpenHashMap<>(textures));
        }
    }
}
