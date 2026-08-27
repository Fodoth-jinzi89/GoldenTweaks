package net.fodoth.skina.goldentweaks.compat.aeallpattern;

import appeng.api.crafting.IPatternDetails;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.util.List;

public final class TechStartPatternCompat {
    private static final Method EXPAND = findExpandMethod();

    private TechStartPatternCompat() {
    }

    @SuppressWarnings("unchecked")
    public static List<IPatternDetails> expand(ItemStack stack, Level level) {
        if (EXPAND == null) {
            return List.of();
        }
        try {
            return (List<IPatternDetails>) EXPAND.invoke(null, stack, level);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return List.of();
        }
    }

    private static Method findExpandMethod() {
        try {
            return Class.forName("com.wuxiaoya.techstart.integration.ae2.TechStartPatternExpansion")
                    .getMethod("expand", ItemStack.class, Level.class);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}
