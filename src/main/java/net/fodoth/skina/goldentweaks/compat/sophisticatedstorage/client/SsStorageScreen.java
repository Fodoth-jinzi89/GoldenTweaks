package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.client;

import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SsStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.LimitedBarrelScreen;

/** 移动结构上的 SS 容器界面：直接复用 SS 的存储屏幕基类（升级栏 + 设置标签），限类桶沿用其 1~4 槽布局。 */
public class SsStorageScreen extends StorageScreenBase<SsStorageMenu> {

    private static final int LIMITED_BARREL_STORAGE_HEIGHT = 82;

    public SsStorageScreen(SsStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private int storageSlots() {
        return this.getMenu().getNumberOfStorageInventorySlots();
    }

    @Override
    protected String getStorageSettingsTabTooltip() {
        return Component.translatable("gui.sophisticatedstorage.settings.tooltip").getString();
    }

    @Override
    protected int getStorageInventoryHeight(int displayableNumberOfRows) {
        return storageSlots() <= 4 ? LIMITED_BARREL_STORAGE_HEIGHT : super.getStorageInventoryHeight(displayableNumberOfRows);
    }

    @Override
    protected void updateStorageSlotsPositions() {
        if (storageSlots() <= 4) {
            LimitedBarrelScreen.updateSlotPositions(this.getMenu(), storageSlots(), this.imageWidth);
        } else {
            super.updateStorageSlotsPositions();
        }
    }

    @Override
    protected void drawSlotBg(GuiGraphics guiGraphics, int x, int y, int visibleSlotsCount) {
        if (storageSlots() <= 4) {
            LimitedBarrelScreen.drawSlotBg(this, guiGraphics, x, y, storageSlots());
        } else {
            super.drawSlotBg(guiGraphics, x, y, visibleSlotsCount);
        }
    }

    @Override
    protected boolean shouldShowSortButtons() {
        return storageSlots() > 4 && super.shouldShowSortButtons();
    }

    @Override
    protected void addSearchBox() {
        if (storageSlots() > 4) {
            super.addSearchBox();
        }
    }
}
