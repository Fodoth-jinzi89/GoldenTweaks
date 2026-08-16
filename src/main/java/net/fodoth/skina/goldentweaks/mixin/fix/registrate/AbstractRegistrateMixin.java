package net.fodoth.skina.goldentweaks.mixin.fix.registrate;

import com.tterrag.registrate.AbstractRegistrate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AbstractRegistrate.class, remap = false)
public abstract class AbstractRegistrateMixin {

    @Redirect(
            method = "onRegister",
            at = @At(value = "INVOKE",
                    target = "Lcom/tterrag/registrate/AbstractRegistrate;isDevEnvironment()Z")
    )
    private boolean gt$allowUnusedRegisterCallbacks() {
        return false;
    }
}
