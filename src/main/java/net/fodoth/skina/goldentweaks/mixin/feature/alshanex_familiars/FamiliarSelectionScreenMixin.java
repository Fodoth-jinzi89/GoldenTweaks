package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import com.mojang.blaze3d.vertex.PoseStack;
import net.alshanex.familiarslib.data.PlayerFamiliarData;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.alshanex.familiarslib.network.ReleaseFamiliarPacket;
import net.alshanex.familiarslib.network.SelectFamiliarPacket;
import net.alshanex.familiarslib.registry.AttachmentRegistry;
import net.alshanex.familiarslib.screen.FamiliarSelectionScreen;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GTConfirmReleaseScreen;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GTFamiliarEntry;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableHelper;
import net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars.accessor.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Mixin(FamiliarSelectionScreen.class)
public abstract class FamiliarSelectionScreenMixin {

    @Shadow @Final private List<?> familiarEntries;

    @Shadow private UUID selectedFamiliarId;
    @Shadow private int maxScroll;
    @Shadow private int scrollOffset;
    @Shadow private int rightPanelX;
    @Shadow private int leftPanelX;
    @Shadow private int panelY;

    @Shadow
    private void enableScissor(int x1, int y1, int x2, int y2) {}

    @Shadow
    private void disableScissor() {}

    @Shadow
    private void renderEntity(GuiGraphics guiGraphics, LivingEntity entity, float x, float y, float z) {}

    @Shadow
    private void drawHeartIcon(GuiGraphics guiGraphics, int x, int y) {}

    @Shadow
    private void drawArmorIcon(GuiGraphics guiGraphics, int x, int y) {}

    @Shadow
    private boolean isFamiliarSummoned(UUID familiarId) {
        return false;
    }

    @Shadow
    private void drawItemIcon(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y) {}

    @Shadow
    private void updateReleaseButtonVisibility() {}

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

        selectedFamiliarId = data.getSelectedFamiliarId();

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
                            : (float) familiar.getBaseMaxHealth();

            float health = nbt.getFloat("currentHealth");

            if (health <= 0) {
                health = baseMaxHealth;
            }

            var gtData = GoldenTweaksConsumableHelper.getData(familiar);

            int armor = (int) gtData.getArmor();
            int enraged = (int) gtData.getEnraged();

            boolean canBlock = gtData.getBlocking() > 0;

            ((List<Object>) familiarEntries).add(
                    new GTFamiliarEntry(
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

        int visibleItems = 3;

        maxScroll = Math.max(
                0,
                (familiarEntries.size() - visibleItems) * 80
        );

        scrollOffset = Math.clamp(
                scrollOffset,
                0,
                maxScroll
        );
    }

    /**
     * @author Fodoth_jinzi89
     * @reason use Golden Tweaks data
     */
    @Overwrite
    public void reloadFamiliarData() {
        loadFamiliarData();
    }

    /**
     * @author Fodoth_jinzi89
     * @reason use Golden Tweaks data
     */
    @Overwrite
    private void renderFamiliarList(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        int scissorX = this.rightPanelX;
        int scissorY = this.panelY;
        int scissorWidth = 200;
        int scissorHeight = 300;

        this.enableScissor(
                scissorX,
                scissorY,
                scissorX + scissorWidth,
                scissorY + scissorHeight
        );

        int currentY = this.panelY - this.scrollOffset;

        for (Object obj : this.familiarEntries) {

            if (!(obj instanceof GTFamiliarEntry entry)) {
                continue;
            }

            if (currentY + 80 > this.panelY
                    && currentY < this.panelY + 300) {

                this.renderFamiliarItem(
                        guiGraphics,
                        entry.id(),
                        entry.familiar(),
                        entry.displayName(),
                        entry.health(),
                        entry.armor(),
                        entry.enraged(),
                        entry.canBlock(),
                        this.rightPanelX,
                        currentY,
                        mouseX,
                        mouseY,
                        partialTick
                );
            }

            currentY += 80;
        }

        this.disableScissor();
    }

    @Unique
    private void renderFamiliarItem(
            GuiGraphics guiGraphics,
            UUID id,
            AbstractSpellCastingPet familiar,
            Component displayName,
            float health,
            int armor,
            int enraged,
            boolean canBlock,
            int x,
            int y,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        PoseStack poseStack = guiGraphics.pose();

        boolean isSelected = id.equals(this.selectedFamiliarId);

        boolean isHovered =
                mouseX >= x && mouseX < x + 200
                        && mouseY >= y && mouseY < y + 80;

        if (isSelected) {
            guiGraphics.fill(x, y, x + 200, y + 80, 1140915968);
        } else if (isHovered) {
            guiGraphics.fill(x, y, x + 200, y + 80, 1157627903);
        }

        poseStack.pushPose();

        poseStack.translate(x + 40, y + 50, 50);

        poseStack.scale(30F, 30F, 30F);

        this.renderEntity(guiGraphics, familiar, 0, 0, 0);

        poseStack.popPose();

        Font font =
                ((ScreenAccessor) this).goldentweaks$getFont();

        guiGraphics.drawString(
                font,
                displayName,
                x + 85,
                y + 20,
                0xFFFFFF
        );

        this.drawHeartIcon(guiGraphics, x + 85, y + 35);

        guiGraphics.drawString(
                font,
                Component.literal(String.format("%.0f", health)),
                x + 97,
                y + 35,
                0xFF6666
        );

        this.drawArmorIcon(guiGraphics, x + 85, y + 49);

        guiGraphics.drawString(
                font,
                Component.literal(String.valueOf(armor)),
                x + 97,
                y + 50,
                0xAAAAAA
        );
    }

    @Unique
    private GTFamiliarEntry getSelectedEntry() {

        if (this.selectedFamiliarId == null) {
            return null;
        }

        for (Object obj : this.familiarEntries) {

            if (!(obj instanceof GTFamiliarEntry entry)) {
                continue;
            }

            if (entry.id().equals(this.selectedFamiliarId)) {
                return entry;
            }
        }

        return null;
    }

    @Inject(
            method = "onReleaseButtonPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gt$onReleaseButtonPressed(
            Button button,
            CallbackInfo ci
    ) {


        if (this.selectedFamiliarId == null) {
            return;
        }

        FamiliarSelectionScreen self =
                (FamiliarSelectionScreen) (Object) this;

        GTFamiliarEntry selectedEntry = getSelectedEntry();

        if (selectedEntry == null) {
            return;
        }

        Component familiarName = selectedEntry.displayName();


        Component title =
                Component.translatable(
                        "ui.familiarslib.confirm_release"
                );

        Component message =
                Component.translatable(
                        "ui.familiarslib.confirm_release_message",
                        familiarName
                );

        Minecraft.getInstance().setScreen(
                new GTConfirmReleaseScreen(
                        self,
                        title,
                        message,
                        confirmed -> {


                            if (confirmed) {


                                PacketDistributor.sendToServer(
                                        new ReleaseFamiliarPacket(
                                                this.selectedFamiliarId
                                        )
                                );

                            }
                        }
                )
        );


        ci.cancel();
    }

    /**
     * @author Fodoth_jinzi89
     * @reason use Golden Tweaks data
     */
    @Overwrite
    private void renderSelectedFamiliar(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        GTFamiliarEntry selectedEntry = this.getSelectedEntry();

        if (selectedEntry == null) {
            return;
        }

        PoseStack poseStack = guiGraphics.pose();

        Font font =
                ((ScreenAccessor) this).goldentweaks$getFont();

        if (this.isFamiliarSummoned(selectedEntry.id())) {

            Component statusComponent =
                    Component.translatable(
                            "ui.familiarslib.familiar_summoned"
                    );

            int statusWidth = font.width(statusComponent);

            guiGraphics.drawString(
                    font,
                    statusComponent,
                    this.leftPanelX + (200 - statusWidth) / 2,
                    this.panelY + 20,
                    16777045
            );
        }

        poseStack.pushPose();

        poseStack.translate(
                this.leftPanelX + 100.0F,
                this.panelY + 100.0F,
                100.0F
        );

        poseStack.scale(60.0F, 60.0F, 60.0F);

        Quaternionf rotation =
                new Quaternionf().rotateY(
                        (float) Math.toRadians(45.0F)
                );

        poseStack.mulPose(rotation);

        this.renderEntity(
                guiGraphics,
                selectedEntry.familiar(),
                0.0F,
                0.0F,
                0.0F
        );

        poseStack.popPose();

        int infoY = this.panelY + 120;

        Component nameComponent =
                selectedEntry.displayName();

        int nameWidth = font.width(nameComponent);

        guiGraphics.drawString(
                font,
                nameComponent,
                this.leftPanelX + (200 - nameWidth) / 2,
                infoY,
                0xFFFFFF
        );

        int boxX = this.leftPanelX + 40;
        int boxY = infoY + 25;
        int boxWidth = 120;
        int boxHeight = 60;

        guiGraphics.fill(
                boxX,
                boxY,
                boxX + boxWidth,
                boxY + boxHeight,
                0xB3000000
        );

        guiGraphics.fill(
                boxX,
                boxY,
                boxX + boxWidth,
                boxY + 1,
                0xFF888888
        );

        guiGraphics.fill(
                boxX,
                boxY + boxHeight - 1,
                boxX + boxWidth,
                boxY + boxHeight,
                0xFF888888
        );

        guiGraphics.fill(
                boxX,
                boxY,
                boxX + 1,
                boxY + boxHeight,
                0xFF888888
        );

        guiGraphics.fill(
                boxX + boxWidth - 1,
                boxY,
                boxX + boxWidth,
                boxY + boxHeight,
                0xFF888888
        );

        int halfWidth = boxWidth / 2;
        int halfHeight = boxHeight / 2;

        Component healthComponent =
                Component.literal(
                        String.format("%.0f", selectedEntry.health())
                );

        int healthTextWidth = font.width(healthComponent);

        int healthTotalWidth = 11 + healthTextWidth;

        int healthStartX =
                boxX + (halfWidth - healthTotalWidth) / 2;

        int healthPosY =
                boxY + (halfHeight - 9) / 2;

        this.drawHeartIcon(
                guiGraphics,
                healthStartX,
                healthPosY
        );

        guiGraphics.drawString(
                font,
                healthComponent,
                healthStartX + 11,
                healthPosY,
                0xFFAA3333
        );

        Component armorComponent =
                Component.literal(
                        String.valueOf(selectedEntry.armor())
                );

        int armorTextWidth = font.width(armorComponent);

        int armorTotalWidth = 11 + armorTextWidth;

        int armorStartX =
                boxX + halfWidth
                        + (halfWidth - armorTotalWidth) / 2;

        int armorY =
                boxY + (halfHeight - 9) / 2;

        this.drawArmorIcon(
                guiGraphics,
                armorStartX,
                armorY
        );

        guiGraphics.drawString(
                font,
                armorComponent,
                armorStartX + 11,
                armorY,
                0xFFAAAAAA
        );

        Component blockComponent =
                Component.literal(
                        selectedEntry.canBlock() ? "1" : "0"
                );

        int blockTextWidth = font.width(blockComponent);

        int blockTotalWidth = 18 + blockTextWidth;

        int blockStartX =
                boxX + (halfWidth - blockTotalWidth) / 2;

        int blockY =
                boxY + halfHeight
                        + (halfHeight - 16) / 2;

        ItemStack shield = new ItemStack(Items.SHIELD);

        guiGraphics.renderItem(
                shield,
                blockStartX,
                blockY
        );

        int blockColor =
                selectedEntry.canBlock()
                        ? 5635925
                        : 16733525;

        guiGraphics.drawString(
                font,
                blockComponent,
                blockStartX + 18,
                blockY + 4,
                blockColor
        );

        Component enragedComponent =
                Component.literal(
                        String.valueOf(selectedEntry.enraged())
                );

        int enragedTextWidth = font.width(enragedComponent);

        int enragedTotalWidth = 18 + enragedTextWidth;

        int enragedStartX =
                boxX + halfWidth
                        + (halfWidth - enragedTotalWidth) / 2;

        int enragedY =
                boxY + halfHeight
                        + (halfHeight - 16) / 2;

        ResourceLocation mu =
                ResourceLocation.withDefaultNamespace(
                        "textures/item/red_mushroom.png"
                );

        this.drawItemIcon(
                guiGraphics,
                mu,
                enragedStartX,
                enragedY
        );

        guiGraphics.drawString(
                font,
                enragedComponent,
                enragedStartX + 18,
                enragedY + 4,
                0xFFAA33FF
        );
    }

    @Inject(
            method = "mouseClicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gt$mouseClicked(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (button == 0
                && mouseX >= (double) this.rightPanelX
                && mouseX < (double) (this.rightPanelX + 200)
                && mouseY >= (double) this.panelY
                && mouseY < (double) (this.panelY + 300)) {

            int relativeY =
                    (int) (
                            mouseY
                                    - (double) this.panelY
                                    + (double) this.scrollOffset
                    );

            int itemIndex = relativeY / 80;

            if (itemIndex >= 0
                    && itemIndex < this.familiarEntries.size()) {

                Object obj =
                        this.familiarEntries.get(itemIndex);

                if (!(obj instanceof GTFamiliarEntry selected)) {
                    return;
                }

                this.selectedFamiliarId = selected.id();

                PacketDistributor.sendToServer(
                        new SelectFamiliarPacket(
                                selected.id()
                        )
                );

                this.updateReleaseButtonVisibility();

                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }
}