package net.fodoth.skina.goldentweaks.compat.neoecoae;

import appeng.api.storage.cells.StorageCell;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import net.minecraft.world.item.ItemStack;

public final class NeoEcoAeStorageCompat {

    private NeoEcoAeStorageCompat() {
    }

    public static boolean isStorageCell(ItemStack stack) {
        return ECOStorageCells.isCellHandled(stack);
    }

    public static StorageCell getCellInventory(ItemStack stack) {
        return ECOStorageCells.getCellInventory(stack, null);
    }
}
