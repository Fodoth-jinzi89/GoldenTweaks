package net.fodoth.skina.goldentweaks.mixin.fix.ftbquests;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(
        targets = "dev.ftb.mods.ftbquests.quest.translation.TranslationManager",
        priority = 500,
        remap = false
)
public abstract class TranslationManagerDummyMixin {
}
