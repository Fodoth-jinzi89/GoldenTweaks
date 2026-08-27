package net.fodoth.skina.goldentweaks.compat.ae2autopatternupload;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.menu.slot.RestrictedInputSlot;
import com.gali.ae2_auto_pattern_upload.network.PatternUploadUtil;
import net.fodoth.skina.goldentweaks.mixin.fix.ae2peat.accessor.PatternEncodingAccessTermMenuAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

import java.util.ArrayList;
import java.util.List;

public final class PeatPatternUploadCompat {
    private PeatPatternUploadCompat() {
    }

    public static List<PatternUploadUtil.ProviderEntry> listProviders(PatternEncodingAccessTermMenu menu) {
        List<PatternUploadUtil.ProviderEntry> providers = new ArrayList<>();
        if (menu.getGridNode() == null) {
            return providers;
        }
        IGrid grid = menu.getGridNode().getGrid();
        for (Class<?> machineClass : grid.getMachineClasses()) {
            if (!PatternContainer.class.isAssignableFrom(machineClass)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Class<? extends PatternContainer> providerClass = (Class<? extends PatternContainer>) machineClass;
            for (PatternContainer provider : grid.getActiveMachines(providerClass)) {
                InternalInventory inventory = provider.getTerminalPatternInventory();
                int emptySlots = countEmptySlots(inventory);
                if (provider.isVisibleInTerminal() && emptySlots > 0) {
                    Component name = provider.getTerminalGroup() == null
                            ? Component.translatable("ae2_auto_pattern_upload.provider.default")
                            : provider.getTerminalGroup().name();
                    providers.add(new PatternUploadUtil.ProviderEntry(provider, name, emptySlots));
                }
            }
        }
        return providers;
    }

    public static boolean upload(PatternEncodingAccessTermMenu menu, int index) {
        List<PatternUploadUtil.ProviderEntry> providers = listProviders(menu);
        RestrictedInputSlot slot = ((PatternEncodingAccessTermMenuAccessor) menu).goldentweaks$getEncodedPatternSlot();
        ItemStack pattern = slot.getItem();
        if (index < 0 || index >= providers.size() || !PatternDetailsHelper.isEncodedPattern(pattern)) {
            return false;
        }
        InternalInventory inventory = providers.get(index).provider().getTerminalPatternInventory();
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                inventory.setItemDirect(i, pattern.copyWithCount(1));
                slot.set(ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    private static int countEmptySlots(InternalInventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
