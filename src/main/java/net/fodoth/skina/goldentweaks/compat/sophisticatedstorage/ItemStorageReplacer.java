package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import net.minecraft.core.BlockPos;

/** 由 {@code MountedStorageManagerMixin} 实现：替换装置上单个位置的挂载物品存储（Create 无公开 API）。 */
public interface ItemStorageReplacer {

    void gt$replaceItemStorage(BlockPos pos, MountedItemStorage replacement);
}
