package net.fodoth.skina.goldentweaks.compat.ae2autopatternupload;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public final class ProviderIconCache {
    private static Map<String, ItemStack> icons = Map.of();

    private ProviderIconCache() {
    }

    public static void set(List<String> names, List<ItemStack> providerIcons) {
        Map<String, ItemStack> newIcons = new HashMap<>();
        for (int i = 0; i < Math.min(names.size(), providerIcons.size()); i++) {
            newIcons.putIfAbsent(names.get(i), providerIcons.get(i));
        }
        icons = Map.copyOf(newIcons);
    }

    public static ItemStack get(String name) {
        return icons.getOrDefault(name, ItemStack.EMPTY);
    }
}
