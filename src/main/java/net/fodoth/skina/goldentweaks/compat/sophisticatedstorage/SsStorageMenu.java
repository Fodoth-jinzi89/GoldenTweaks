package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;

/** 移动结构上的精致存储容器使用的 SS 原生存储菜单（带升级栏；设置标签由屏幕基类提供）。 */
public class SsStorageMenu extends StorageContainerMenuBase<IStorageWrapper> implements ISyncedContainer {

    private static final String SOURCE_CONTAINER_ID_TAG = "sourceContainerId";

    private final int contraptionEntityId;
    private final BlockPos localPos;

    public SsStorageMenu(MenuType<?> menuType, int containerId, Player player, IStorageWrapper storageWrapper,
                         int contraptionEntityId, BlockPos localPos) {
        super(menuType, containerId, player, storageWrapper, NoopStorageWrapper.INSTANCE, -1, false);
        this.contraptionEntityId = contraptionEntityId;
        this.localPos = localPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    @Override
    public Optional<BlockPos> getBlockPosition() {
        return Optional.empty();
    }

    @Override
    public Optional<Entity> getEntity() {
        return Optional.empty();
    }

    @Override
    protected StorageUpgradeSlot instantiateUpgradeSlot(UpgradeHandler upgradeHandler, int slotIndex) {
        return new StorageUpgradeSlot(upgradeHandler, slotIndex) {
            @Override
            protected void onUpgradeChanged() {
            }
        };
    }

    // 限类桶物品槽上的红绿叠加层来自基类的 getSlotOverlayColors（它直接读 wrapper 的 ISlotColorCategory），
    // 与槽位信息同步无关。SS 自己的 LimitedBarrelContainerMenu 就是返回空列表，这里对齐即可。
    @Override
    public List<Integer> getSlotOverlayColors(int slot) {
        return List.of();
    }

    // 设置按钮：客户端请求服务端打开设置界面；服务端用挂载存储的 wrapper 构建 SsStorageSettingsMenu。
    @Override
    public void openSettings() {
        if (player.level().isClientSide()) {
            sendToServer(data -> {
                data.putString(ACTION_TAG, "openSettings");
                data.putInt(SOURCE_CONTAINER_ID_TAG, containerId);
            });
            return;
        }
        player.openMenu(
                new SimpleMenuProvider(
                        (windowId, inventory, menuPlayer) -> new SsStorageSettingsMenu(
                                SsStorageMenus.settingsType(), windowId, menuPlayer, getStorageWrapper(),
                                contraptionEntityId, localPos),
                        Component.translatable("gui.sophisticatedstorage.settings.title")),
                buffer -> {
                    buffer.writeVarInt(contraptionEntityId);
                    buffer.writeBlockPos(localPos);
                });
    }

    // 与 SS 一致：忽略来自其它容器实例的 openSettings 请求，避免串台
    @Override
    public void handlePacket(CompoundTag data) {
        if ("openSettings".equals(data.getString(ACTION_TAG))
                && (!data.contains(SOURCE_CONTAINER_ID_TAG) || data.getInt(SOURCE_CONTAINER_ID_TAG) != containerId)) {
            return;
        }
        super.handlePacket(data);
    }

    @Override
    protected boolean storageItemHasChanged() {
        return false;
    }

    @Override
    public boolean detectSettingsChangeAndReload() {
        return false;
    }
}
