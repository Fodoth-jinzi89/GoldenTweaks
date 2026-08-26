package net.fodoth.skina.goldentweaks.mixin.fix.aeallpattern;

import io.github.langqi99.aeallpattern.binding.BindingValidator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(BindingValidator.Context.class)
public class ContextMixin {

    @ModifyReturnValue(
            method = "sameDimension",
            at = @At("RETURN")
    )
    private boolean modifySameDimension(boolean original) {
        return true;
    }

    @ModifyReturnValue(
            method = "withinRange",
            at = @At("RETURN")
    )
    private boolean modifyWithinRange(boolean original) {
        return true;
    }
}
