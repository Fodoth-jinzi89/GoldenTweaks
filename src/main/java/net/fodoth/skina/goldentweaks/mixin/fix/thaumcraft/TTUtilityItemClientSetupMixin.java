package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumic.tinkerer.client.TTUtilityItemClientSetup;
import thaumic.tinkerer.common.item.MobAspectItem;

@Mixin(value = TTUtilityItemClientSetup.class, remap = false)
public class TTUtilityItemClientSetupMixin {

    @Inject(
            method = "mobAspectIndex",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void fixNullAspect(ItemStack stack,
                                      CallbackInfoReturnable<Float> cir) {

        if (MobAspectItem.aspect(stack) == null) {
            cir.setReturnValue(0.0F);
        }
    }
}