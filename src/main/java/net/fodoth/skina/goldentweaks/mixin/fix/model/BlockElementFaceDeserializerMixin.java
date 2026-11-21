package net.fodoth.skina.goldentweaks.mixin.fix.model;

import net.fodoth.skina.goldentweaks.util.GTModelTextureNames;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockElementFace.Deserializer.class)
public abstract class BlockElementFaceDeserializerMixin {
    @Inject(method = "getTexture", at = @At("RETURN"), cancellable = true)
    private void gt$shareTextureName(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(GTModelTextureNames.share(cir.getReturnValue()));
    }
}
