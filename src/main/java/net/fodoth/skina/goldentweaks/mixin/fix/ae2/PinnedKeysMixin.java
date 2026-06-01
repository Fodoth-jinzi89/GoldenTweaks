package net.fodoth.skina.goldentweaks.mixin.fix.ae2;

import appeng.client.gui.me.common.PinnedKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = PinnedKeys.class, remap = false)
public class PinnedKeysMixin {

    @ModifyArg(
            method = "pinKey",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/ArrayList;subList(II)Ljava/util/List;"
            ),
            index = 1
    )
    private static int goldentweaks$fixNegativeSubListIndex(int original) {
        return -original;
    }
}