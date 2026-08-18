package net.fodoth.skina.goldentweaks.mixin.fix.carryon;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(
        targets = "tschipp.carryon.common.carry.PickupHandler",
        priority = 1100,
        remap = false
)
public abstract class PickupHandlerDummyMixin {
}
