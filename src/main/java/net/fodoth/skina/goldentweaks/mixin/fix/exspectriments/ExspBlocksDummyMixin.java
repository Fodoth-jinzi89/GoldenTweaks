package net.fodoth.skina.goldentweaks.mixin.fix.exspectriments;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = {
        "io.github.chromonym.exspectriments.ExspBlocks",
        "io.github.chromonym.exspectriments.entities.PrinterBlockEntity",
        "io.github.chromonym.exspectriments.screenhandlers.PrinterScreenHandler"
}, remap = false)
public abstract class ExspBlocksDummyMixin {
}
