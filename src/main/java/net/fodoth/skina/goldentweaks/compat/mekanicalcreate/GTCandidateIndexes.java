package net.fodoth.skina.goldentweaks.compat.mekanicalcreate;

import org.jetbrains.annotations.NotNull;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class GTCandidateIndexes {

    private GTCandidateIndexes() {
    }

    public static <K, V> Map<K, List<V>> compactSingletons(@NotNull Map<K, List<V>> index) {
        Map<V, List<V>> singletons = new IdentityHashMap<>();
        index.replaceAll((item, candidates) -> {
            if (candidates.size() != 1) {
                return candidates;
            }
            return singletons.computeIfAbsent(candidates.getFirst(), candidate -> candidates);
        });
        return index;
    }
}
