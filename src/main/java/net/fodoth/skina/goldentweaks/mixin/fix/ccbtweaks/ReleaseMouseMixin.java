package net.fodoth.skina.goldentweaks.mixin.fix.ccbtweaks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.infpancakefactory.ccb.tweaks.dyn.client.hudactions.ReleaseMouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ReleaseMouse.class)
public class ReleaseMouseMixin {

    @WrapOperation(
            method = "releaseMouseLogic",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/ModConfigSpec$BooleanValue;get()Ljava/lang/Object;"
            )
    )
    private static Object gt$safeGet(
            ModConfigSpec.BooleanValue instance, Operation<Object> original
    ) {
        try {
            return original.call(instance);
        } catch (IllegalStateException e) {
            return false;
        }
    }
}