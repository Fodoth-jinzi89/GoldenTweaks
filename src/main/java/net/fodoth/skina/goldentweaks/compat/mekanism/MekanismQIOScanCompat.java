package net.fodoth.skina.goldentweaks.compat.mekanism;

import mekanism.common.attachments.qio.DriveContents;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

public final class MekanismQIOScanCompat {

    private MekanismQIOScanCompat() {
    }

    public static boolean isDrive(ItemStack stack) {
        return stack.getItem() instanceof IQIODriveItem;
    }

    public static Iterator<ItemStack> getContents(ItemStack stack) {
        DriveContents contents = stack.getOrDefault(MekanismDataComponents.DRIVE_CONTENTS, DriveContents.EMPTY);
        return contents.namedItemMap().keySet().stream()
                .map(QIOGlobalItemLookup.INSTANCE::getTypeByUUID)
                .filter(type -> type != null)
                .map(type -> type.createStack(1))
                .iterator();
    }
}
