package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import com.mojang.blaze3d.vertex.PoseStack;
import net.alshanex.familiarslib.data.PlayerFamiliarData;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.alshanex.familiarslib.network.UpdateMultiSelectionCurioPacket;
import net.alshanex.familiarslib.registry.AttachmentRegistry;
import net.alshanex.familiarslib.screen.MultiSelectionCurioScreen;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GTMultiSelectionEntry;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableHelper;
import net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars.accessor.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(MultiSelectionCurioScreen.class)
public class MultiSelectionCurioScreenMixin {
    @Shadow
    @Final
    private List<?> familiarEntries;

    @Shadow @Final
    private Set<UUID> selectedFamiliars;

    @Shadow private int scrollOffset;
    @Shadow private int maxScroll;

    @Shadow private int gridStartX;
    @Shadow private int gridStartY;


    @Shadow
    private void renderEntity(
            GuiGraphics guiGraphics,
            LivingEntity entity,
            float x,
            float y,
            float z
    ) {}

    @Shadow
    private void drawHeartIcon(
            GuiGraphics guiGraphics,
            int x,
            int y
    ) {}

    @Shadow
    private void drawArmorIcon(
            GuiGraphics guiGraphics,
            int x,
            int y
    ) {}

    @Shadow
    private void drawItemIcon(
            GuiGraphics guiGraphics,
            ResourceLocation texture,
            int x,
            int y
    ) {}

    /**
     * @author Fodoth_jinzi89
     * @reason use Golden Tweaks data
     */
    @Overwrite
    @SuppressWarnings("unchecked")
    private void loadFamiliarData() {

        Minecraft minecraft = Minecraft.getInstance();

        familiarEntries.clear();

        if (minecraft.player == null) {
            return;
        }

        PlayerFamiliarData data =
                minecraft.player.getData(
                        AttachmentRegistry.PLAYER_FAMILIAR_DATA
                );


        Map<UUID, CompoundTag> familiars = data.getAllFamiliars();

        for (Map.Entry<UUID, CompoundTag> entry : familiars.entrySet()) {

            UUID id = entry.getKey();
            CompoundTag nbt = entry.getValue();

            String entityTypeString = nbt.getString("id");

            EntityType<?> entityType =
                    EntityType.byString(entityTypeString).orElse(null);

            if (entityType == null) {
                continue;
            }

            Entity entity = null;

            if (minecraft.level != null) {
                entity = entityType.create(minecraft.level);
            }

            if (!(entity instanceof AbstractSpellCastingPet familiar)) {
                continue;
            }

            familiar.load(nbt);
            familiar.setUUID(id);

            GoldenTweaksConsumableHelper.loadDataIntoCache(familiar, nbt);
            if (!minecraft.level.isClientSide){
                GoldenTweaksConsumableHelper.sync(familiar);
            }

            Component displayName;

            if (nbt.contains("CustomName", Tag.TAG_STRING)
                    && minecraft.level != null) {

                Component parsed = Component.Serializer.fromJson(
                        nbt.getString("CustomName"),
                        minecraft.level.registryAccess()
                );

                displayName = parsed != null
                        ? parsed
                        : familiar.getType().getDescription();

            } else if (familiar.hasCustomName()) {

                displayName = Objects.requireNonNull(
                        familiar.getCustomName()
                );

            } else {

                displayName = familiar.getType().getDescription();
            }

            float baseMaxHealth =
                    nbt.contains("baseMaxHealth")
                            ? nbt.getFloat("baseMaxHealth")
                            : familiar.getBaseMaxHealth();

            float health = nbt.getFloat("currentHealth");

            if (health <= 0) {
                health = baseMaxHealth;
            }

            var gtData = GoldenTweaksConsumableHelper.getData(familiar);

            int armor = (int) gtData.getArmor();
            int enraged = (int) gtData.getEnraged();

            boolean canBlock = gtData.getBlocking() > 0;

            ((List<Object>) familiarEntries).add(
                    new GTMultiSelectionEntry(
                            id,
                            familiar,
                            displayName,
                            health,
                            armor,
                            enraged,
                            canBlock,
                            baseMaxHealth
                    )
            );
        }

        int totalRows =
                (int) Math.ceil(
                        familiarEntries.size() / 3.0
                );

        maxScroll =
                Math.max(
                        0,
                        (totalRows - 2) * 120
                );
    }

    /**
     * @author Fodoth_jinzi89
     * @reason Use GoldenTweaks System
     */
    @Overwrite
    private void renderFamiliarGrid(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        int index = 0;

        for (Object obj : familiarEntries) {

            if (!(obj instanceof GTMultiSelectionEntry entry)) {
                continue;
            }

            int col = index % 3;
            int row = index / 3;

            int cellX =
                    gridStartX + col * 120;

            int cellY =
                    gridStartY
                            + row * 120
                            - scrollOffset;

            gt$renderFamiliarCell(
                    guiGraphics,
                    entry,
                    cellX,
                    cellY,
                    mouseX,
                    mouseY,
                    partialTick
            );

            index++;
        }
    }

    @Unique
    private void gt$renderFamiliarCell(
            GuiGraphics guiGraphics,
            GTMultiSelectionEntry entry,
            int x,
            int y,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        PoseStack poseStack = guiGraphics.pose();
        boolean isSelected = this.selectedFamiliars.contains(entry.id());
        boolean cellVisible = y + 120 > this.gridStartY && y < this.gridStartY + 240;
        boolean isHovered = cellVisible && mouseX >= x + 5 && mouseX < x + 120 - 5 && mouseY >= y + 5 && mouseY < y + 120 - 5;
        int backgroundColor = 1140850688;
        if (isSelected) {
            backgroundColor = 1140915968;
        } else if (isHovered) {
            backgroundColor = 1157627903;
        }

        guiGraphics.fill(x + 5, y + 5, x + 120 - 5, y + 120 - 5, backgroundColor);
        int borderColor = isSelected ? -16711936 : (isHovered ? -1 : -10066330);
        guiGraphics.renderOutline(x + 5, y + 5, 110, 110, borderColor);
        poseStack.pushPose();
        poseStack.translate((float)(x + 60), (float)(y + 45), 50.0F);
        poseStack.scale(25.0F, 25.0F, 25.0F);
        Quaternionf rotation = (new Quaternionf()).rotateY((float)Math.toRadians(45.0F));
        poseStack.mulPose(rotation);
        this.renderEntity(guiGraphics, entry.familiar(), 0.0F, 0.0F, 0.0F);
        poseStack.popPose();
        Component displayName = entry.displayName();

        Font font =
                ((ScreenAccessor) this).goldentweaks$getFont();

        if (font.width(displayName) > 100) {

            displayName = Component.literal(
                    font.plainSubstrByWidth(
                            displayName.getString(),
                            95
                    ) + "..."
            );
        }

        int nameWidth = font.width(displayName);
        guiGraphics.drawString(font, displayName, x + (120 - nameWidth) / 2, y + 55, 16777215);
        this.gt$renderAttributesInQuadrants(guiGraphics, entry, x, y + 68);
        if (isSelected) {
            Component checkmark = Component.literal("✓");
            guiGraphics.drawString(font, checkmark, x + 120 - 20, y + 10, 65280);
        }
    }

    @Unique
    private void gt$renderAttributesInQuadrants(GuiGraphics guiGraphics, GTMultiSelectionEntry entry, int x, int y) {
        int boxX = x + 20;
        int boxWidth = 80;
        int boxHeight = 40;
        guiGraphics.fill(boxX, y, boxX + boxWidth, y + boxHeight, -1430208320);
        guiGraphics.fill(boxX, y, boxX + boxWidth, y + 1, -8355712);
        guiGraphics.fill(boxX, y + boxHeight - 1, boxX + boxWidth, y + boxHeight, -8355712);
        guiGraphics.fill(boxX, y, boxX + 1, y + boxHeight, -8355712);
        guiGraphics.fill(boxX + boxWidth - 1, y, boxX + boxWidth, y + boxHeight, -8355712);
        int halfWidth = boxWidth / 2;
        int halfHeight = boxHeight / 2;
        Component healthComponent = Component.literal(String.format("%.0f", entry.health()));
        Font font =
                ((ScreenAccessor) this).goldentweaks$getFont();
        int healthTextWidth = font.width(healthComponent);
        int healthTotalWidth = 11 + healthTextWidth;
        int healthStartX = boxX + (halfWidth - healthTotalWidth) / 2;
        int healthY = y + (halfHeight - 9) / 2;
        this.drawHeartIcon(guiGraphics, healthStartX, healthY);
        guiGraphics.drawString(font, healthComponent, healthStartX + 11, healthY, 16733525);
        Component armorComponent = Component.literal(String.valueOf(entry.armor()));
        int armorTextWidth = font.width(armorComponent);
        int armorTotalWidth = 11 + armorTextWidth;
        int armorStartX = boxX + halfWidth + (halfWidth - armorTotalWidth) / 2;
        int armorY = y + (halfHeight - 9) / 2;
        this.drawArmorIcon(guiGraphics, armorStartX, armorY);
        guiGraphics.drawString(font, armorComponent, armorStartX + 11, armorY, 11184810);
        Component blockComponent = Component.literal(entry.canBlock() ? "1" : "0");
        int blockTextWidth = font.width(blockComponent);
        int blockTotalWidth = 18 + blockTextWidth;
        int blockStartX = boxX + (halfWidth - blockTotalWidth) / 2;
        int blockY = y + halfHeight + (halfHeight - 16) / 2;
        ItemStack shield = new ItemStack(Items.SHIELD);
        guiGraphics.renderItem(shield, blockStartX, blockY);
        int blockColor = entry.canBlock() ? 5635925 : 16733525;
        guiGraphics.drawString(font, blockComponent, blockStartX + 18, blockY + 4, blockColor);
        Component enragedComponent = Component.literal(String.valueOf(entry.enraged()));
        int enragedTextWidth = font.width(enragedComponent);
        int enragedTotalWidth = 18 + enragedTextWidth;
        int enragedStartX = boxX + halfWidth + (halfWidth - enragedTotalWidth) / 2;
        int enragedY = y + halfHeight + (halfHeight - 16) / 2;
        ResourceLocation mu =
                ResourceLocation.withDefaultNamespace(
                        "textures/item/red_mushroom.png"
                );
        this.drawItemIcon(guiGraphics, mu, enragedStartX, enragedY);
        guiGraphics.drawString(font, enragedComponent, enragedStartX + 18, enragedY + 4, 16733695);
    }

    @Inject(
            method = "mouseClicked",
            at = @At("HEAD"),
            cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 0) {
            int index = 0;

            for (Object obj : familiarEntries) {

                if (!(obj instanceof GTMultiSelectionEntry entry)) {
                    continue;
                }

                boolean mouseInCell = gt$isMouseInCell(mouseX, mouseY, index);

                if (mouseInCell) {

                    gt$toggleSelection(entry.id());

                    cir.setReturnValue(true);
                    cir.cancel();

                    return;
                }

                index++;
            }
        }
    }

    @Unique
    private void gt$toggleSelection(UUID familiarId) {

        if (selectedFamiliars.contains(familiarId)) {

            selectedFamiliars.remove(familiarId);

        } else if (selectedFamiliars.size() < 10) {

            selectedFamiliars.add(familiarId);
        }

        PacketDistributor.sendToServer(
                new UpdateMultiSelectionCurioPacket(
                        new HashSet<>(selectedFamiliars)
                )
        );
    }

    @Unique
    private boolean gt$isMouseInCell(double mouseX, double mouseY, int index) {
        int col = index % 3;
        int row = index / 3;

        int cellX =
                gridStartX + col * 120;

        int cellY =
                gridStartY
                        + row * 120
                        - scrollOffset;

        return mouseX >= cellX + 5
                && mouseX < cellX + 115
                && mouseY >= cellY + 5
                && mouseY < cellY + 115;
    }
}
