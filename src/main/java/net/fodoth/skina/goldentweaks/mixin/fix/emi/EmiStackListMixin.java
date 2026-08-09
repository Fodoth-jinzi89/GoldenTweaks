package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.registry.EmiStackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(value = EmiStackList.class, remap = false)
public class EmiStackListMixin {


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