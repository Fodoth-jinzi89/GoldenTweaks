package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.registry.EmiStackList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(value = EmiStackList.class, remap = false)
public class EmiStackListMixin {

    @Inject(
            method = "isHiddenFromRecipeViewers",
            at = @At("HEAD"),
            cancellable = true
    )
    private static <T> void goldentweaks$showTravelopticsItems(T value, CallbackInfoReturnable<Boolean> cir) {
        if (value instanceof Item item) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (id.getNamespace().equals("traveloptics")
                    && !id.getPath().contains("example")
                    && !id.getPath().contains("dummy")) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(
            method = "reload",
            at = @At("TAIL")
    )
    private static void goldentweaks$makeStacksSafe(CallbackInfo ci) {

        EmiStackList.stacks =
                Collections.synchronizedList(
                        EmiStackList.stacks
                );
    }

    @Inject(
            method = "bake",
            at = @At("HEAD")
    )
    private static void goldentweaks$copyStacks(CallbackInfo ci) {

        EmiStackList.stacks =
                new CopyOnWriteArrayList<>(
                        EmiStackList.stacks
                );

    }
}
