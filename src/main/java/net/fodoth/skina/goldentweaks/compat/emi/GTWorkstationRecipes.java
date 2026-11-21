package net.fodoth.skina.goldentweaks.compat.emi;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class GTWorkstationRecipes {

    private GTWorkstationRecipes() {
    }

    public static <K, V> void compact(@NotNull Map<K, List<V>> workstations) {
        Map<IdentityKey<V>, CopyOnWriteArrayList<V>> shared = new HashMap<>();
        workstations.replaceAll((stack, recipes) -> {
            IdentityKey<V> key = new IdentityKey<>(recipes);
            CopyOnWriteArrayList<V> canonical = shared.get(key);
            if (canonical == null) {
                canonical = new CopyOnWriteArrayList<>(recipes);
                shared.put(key, canonical);
            }
            // The JDK shares the array between these lists, but copies it on mutation.
            return new CopyOnWriteArrayList<>(canonical);
        });
    }

    private record IdentityKey<V>(List<V> recipes) {

        @Override
        public int hashCode() {
            int hash = 1;
            for (V recipe : recipes) {
                hash = 31 * hash + System.identityHashCode(recipe);
            }
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof IdentityKey<?> key) || recipes.size() != key.recipes.size()) {
                return false;
            }
            for (int i = 0; i < recipes.size(); i++) {
                if (recipes.get(i) != key.recipes.get(i)) {
                    return false;
                }
            }
            return true;
        }
    }
}
