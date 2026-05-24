package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import net.alshanex.alshanex_familiars.registry.ItemRegistry;
import net.alshanex.alshanex_familiars.screen.MonocleOverlay;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableData;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MonocleOverlay.class)
public class MonocleOverlayMixin {

    /**
     * @author GoldenTweaks
     * @reason Hide vanilla consumable caps in the monocle overlay. GoldenTweaks extends
     * consumable limits, so displaying values as current/max, such as 100/45%, is misleading.
     */
    @Overwrite
    private void renderPetStats(
            GuiGraphics guiGraphics,
            AbstractSpellCastingPet pet,
            Minecraft minecraft
    ) {
        GoldenTweaksConsumableData data =
                GoldenTweaksConsumableHelper.getData(pet);

        double spellPower =
                data.getSpellPower();
        double spellResist =
                data.getSpellResist();

        int screenHeight =
                minecraft.getWindow().getGuiScaledHeight();
        int startY =
                screenHeight / 2 - 20;

        goldentweaks$renderStat(
                guiGraphics,
                minecraft,
                new ItemStack(ItemRegistry.MAGIC_POWER_TIER_1.get()),
                Mth.floor(spellPower),
                startY
        );

        goldentweaks$renderStat(
                guiGraphics,
                minecraft,
                new ItemStack(ItemRegistry.MAGIC_RESIST_TIER_1.get()),
                Mth.floor(spellResist),
                startY + 20
        );
    }

    @Unique
    private void goldentweaks$renderStat(
            GuiGraphics guiGraphics,
            Minecraft minecraft,
            ItemStack icon,
            int value,
            int y
    ) {
        guiGraphics.renderFakeItem(icon, 10, y);
        guiGraphics.drawString(
                minecraft.font,
                Component.literal(value + "%"),
                28,
                y + 4,
                0xFFFFFF,
                true
        );
    }
}
