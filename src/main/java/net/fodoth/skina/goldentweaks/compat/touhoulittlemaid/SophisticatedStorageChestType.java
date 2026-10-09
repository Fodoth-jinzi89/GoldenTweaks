package net.fodoth.skina.goldentweaks.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IChestType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;

public class SophisticatedStorageChestType implements IChestType {

    @Override
    public boolean isChest(BlockEntity chest) {
        return chest instanceof StorageBlockEntity;
    }

    @Override
    public boolean canOpenByPlayer(BlockEntity chest, Player player) {
        return true;
    }

    @Override
    public int getOpenCount(BlockGetter level, BlockPos pos, BlockEntity chest) {
        return chest instanceof StorageBlockEntity storage && storage.isOpen() ? DENY_COUNT : ALLOW_COUNT;
    }
}
