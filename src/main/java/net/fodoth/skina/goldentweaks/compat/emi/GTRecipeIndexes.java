package net.fodoth.skina.goldentweaks.compat.emi;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Collections;

public final class GTRecipeIndexes {
    private GTRecipeIndexes() {
    }

    public static <K, V> @NotNull Map<K, V> compactIds(@NotNull Map<K, V> source) {
        return new Object2ObjectOpenHashMap<>(source);
    }

    public static <K, V> void compactSmallLists(@NotNull Map<K, List<V>> index) {
        // EMI finishes these indexes with Stream.toList(); retain immutable lists and exact element order.
        index.replaceAll((key, values) -> values.size() == 1 ? Collections.singletonList(values.getFirst()) : values);
    }
}
