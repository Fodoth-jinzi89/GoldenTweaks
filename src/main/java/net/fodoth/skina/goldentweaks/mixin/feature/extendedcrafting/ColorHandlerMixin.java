package net.fodoth.skina.goldentweaks.mixin.feature.extendedcrafting;

import com.blakebr0.extendedcrafting.client.handler.ColorHandler;
import com.blakebr0.extendedcrafting.init.ModItems;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Arrays;

@Mixin(ColorHandler.class)
public class ColorHandlerMixin {

    @ModifyArgs(
            method = "onItemColors",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/event/RegisterColorHandlersEvent$Item;register(Lnet/minecraft/client/color/item/ItemColor;[Lnet/minecraft/world/level/ItemLike;)V",
                    ordinal = 1
            )
    )
    private void goldenTweaks$removeUltimateIngotRainbow(Args args) {
        ItemLike[] items = args.get(1);

        ItemLike ingot = ModItems.THE_ULTIMATE_INGOT.get();

        ItemLike[] filtered = Arrays.stream(items)
                .filter(item -> item != ingot)
                .toArray(ItemLike[]::new);

        args.set(1, filtered);
    }
}
