package net.fodoth.skina.goldentweaks.mixin.feature;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(
            method = "canEat",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldenTweaks$alwaysEat(
            boolean ignoreHunger,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (GoldenTweaksCommonConfig.isAlwaysEdible()) {
            cir.setReturnValue(true);
        }
    }
}
