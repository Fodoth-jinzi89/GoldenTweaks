package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;

/**
 * 移动装置上精致存储容器的设置菜单。
 *
 * <p>SS 原生的 {@code StorageSettingsContainerMenu} 会用 (level, pos) 去世界里找方块 BE 再取 wrapper，
 * 装置上没有对应的世界方块。SC 的 {@link SettingsContainerMenu} 基类构造直接接收 wrapper，所以这里
 * 直接用挂载存储的 wrapper 构建，客户端屏幕复用 SS 的 {@code StorageSettingsScreen}。</p>
 */
public class SsStorageSettingsMenu extends SettingsContainerMenu<IStorageWrapper> {

    private final BlockPos pos;
    private final int contraptionEntityId;

    public SsStorageSettingsMenu(MenuType<?> menuType, int windowId, Player player, IStorageWrapper storageWrapper,
                                 int contraptionEntityId, BlockPos pos) {
        super(menuType, windowId, player, storageWrapper);
        this.contraptionEntityId = contraptionEntityId;
        this.pos = pos;
    }

    public int getContraptionEntityId() {
        return contraptionEntityId;
    }

    public BlockPos getLocalPos() {
        return pos;
    }

    @Override
    public BlockPos getBlockPosition() {
        return pos;
    }

    @Override
    public void detectSettingsChangeAndReload() {
        // 装置上不同步联动存储快照
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }
}
