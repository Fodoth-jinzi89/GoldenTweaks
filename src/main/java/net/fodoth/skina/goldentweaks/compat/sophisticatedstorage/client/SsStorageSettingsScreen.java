package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.client;

import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SsStorageSettingsMenu;
import net.fodoth.skina.goldentweaks.network.packet.C2SOpenSsContraptionStoragePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.LimitedBarrelScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 装置上精致存储容器的设置界面：复用 SS 的设置屏幕，并像 SS 的 {@code LimitedBarrelSettingsScreen}
 * 那样在槽位 ≤ 4 时改用限类桶的布局；「返回存储界面」改为重开装置上的挂载存储菜单。
 */
public class SsStorageSettingsScreen extends StorageSettingsScreen {

    public SsStorageSettingsScreen(SettingsContainerMenu<?> menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private int storageSlots() {
        return getMenu().getNumberOfStorageInventorySlots();
    }

    @Override
    protected int getStorageInventoryHeight(int displayableNumberOfRows) {
        return storageSlots() <= 4 ? LimitedBarrelScreen.STORAGE_SLOTS_HEIGHT : super.getStorageInventoryHeight(displayableNumberOfRows);
    }

    @Override
    protected void updateStorageSlotsPositions() {
        if (storageSlots() <= 4) {
            LimitedBarrelScreen.updateSlotPositions(getMenu(), storageSlots(), imageWidth);
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
    protected void sendStorageInventoryScreenOpenMessage() {
        reopenContraptionStorage();
    }

    /**
     * 重开装置上的挂载存储菜单。ESC 与 SS 的「返回存储」页签都会走到这里——
     * 后者默认会发 SS 的原生 {@code OpenStorageInventoryPayload}（按世界方块 pos 打开），
     * 在装置上会打开不存在/错误的方块菜单甚至直接抛错断连，所以必须改成这条路径。
     */
    public void reopenContraptionStorage() {
        if (menu instanceof SsStorageSettingsMenu settingsMenu) {
            PacketDistributor.sendToServer(new C2SOpenSsContraptionStoragePacket(
                    settingsMenu.getContraptionEntityId(), settingsMenu.getLocalPos()));
        }
    }
}
