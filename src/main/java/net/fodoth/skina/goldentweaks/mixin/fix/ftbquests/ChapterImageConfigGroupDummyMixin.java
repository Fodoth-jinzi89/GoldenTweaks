package net.fodoth.skina.goldentweaks.mixin.fix.ftbquests;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(
        targets = "dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton$3",
        priority = Integer.MAX_VALUE,
        remap = false
)
public abstract class ChapterImageConfigGroupDummyMixin {

    private String val$name = "";
}
