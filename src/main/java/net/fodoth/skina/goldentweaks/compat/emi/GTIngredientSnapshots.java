package net.fodoth.skina.goldentweaks.compat.emi;

import java.util.List;

public final class GTIngredientSnapshots {

    private GTIngredientSnapshots() {
    }

    public static <T> List<T> reuse(List<T> original, List<T> copy) {
        if (original.size() != copy.size()) {
            return copy;
        }
        for (int i = 0; i < original.size(); i++) {
            if (original.get(i) != copy.get(i)) {
                return copy;
            }
        }
        return original;
    }
}
