package net.fodoth.skina.goldentweaks.mixin.fix.continuity;

import com.google.common.collect.ImmutableMap;
import net.fodoth.skina.goldentweaks.compat.continuity.GTBlockStateModelLookup;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "me.pepperbell.continuity.client.resource.ModelWrappingHandler", remap = false)
public abstract class ModelWrappingHandlerMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lme/pepperbell/continuity/client/resource/ModelWrappingHandler;createBlockStateModelIdMap()Lcom/google/common/collect/ImmutableMap;"))
    private ImmutableMap<ModelResourceLocation, BlockState> gt$omitStateIndex() {
        return ImmutableMap.of();
    }

    @Redirect(method = "wrap", at = @At(value = "INVOKE",
            target = "Lcom/google/common/collect/ImmutableMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object gt$resolveState(ImmutableMap<?, ?> unused, Object key) {
        return GTBlockStateModelLookup.find(key);
    }
}
