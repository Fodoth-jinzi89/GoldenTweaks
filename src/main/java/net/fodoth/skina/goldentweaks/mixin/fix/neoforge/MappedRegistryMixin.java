package net.fodoth.skina.goldentweaks.mixin.fix.neoforge;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MappedRegistry.class)
public class MappedRegistryMixin {

    @ModifyExpressionValue(
            method = "register(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z",
                    ordinal = 1
            )
    )
    private boolean goldentweaks$ignoreDuplicateValue(boolean original) {
        return false;
    }
}