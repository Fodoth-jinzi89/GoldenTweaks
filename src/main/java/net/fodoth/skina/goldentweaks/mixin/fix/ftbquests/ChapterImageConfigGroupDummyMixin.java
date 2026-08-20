package net.fodoth.skina.goldentweaks.mixin.fix.ftbquests;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(
        targets = "dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton$3",
        priority = 1500,
        remap = false
)
public abstract class ChapterImageConfigGroupDummyMixin {
}
