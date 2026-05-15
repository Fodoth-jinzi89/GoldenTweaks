package net.fodoth.skina.goldentweaks.mixin.feature;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(FoodProperties.class)
public class FoodPropertiesMixin {

    @Final
    @Mutable
    @Shadow
    private boolean canAlwaysEat;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void forceCanAlwaysEat(
            int nutrition,
            float saturation,
            boolean canAlwaysEat,
            float eatSeconds,
            Optional<ItemStack> usingConvertsTo,
            List<FoodProperties.PossibleEffect> effects,
            CallbackInfo ci
    ) {
        this.canAlwaysEat = true;
    }
}