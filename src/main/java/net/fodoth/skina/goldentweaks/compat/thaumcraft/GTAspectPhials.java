package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.items.AspectEssenceItem;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Binds JSON-registered aspects onto vanilla {@link AspectEssenceItem} phials.
 * <p>
 * The phials are registered with {@code fixedAspect == null}; once the aspects
 * have been loaded from JSON, this class reflectively sets {@code fixedAspect}
 * so the phials behave exactly like the vanilla fixed-variant phials.
 */
public final class GTAspectPhials {

    private static final String FIXED_ASPECT_FIELD = "fixedAspect";

    private GTAspectPhials() {
    }

    public static void bind() {
        for (Map.Entry<String, DeferredItem<Item>> entry : GTThaumcraftAdditionalItems.phials().entrySet()) {
            bindFixedAspect(entry.getValue(), entry.getKey());
        }
    }

    private static void bindFixedAspect(DeferredItem<Item> holder, String tag) {
        Aspect aspect = Aspect.get(tag);
        if (aspect == null) {
            GoldenTweaks.LOGGER.warn("Cannot bind phial: aspect '{}' is not registered.", tag);
            return;
        }

        Item item = holder.get();
        if (!(item instanceof AspectEssenceItem phial)) {
            GoldenTweaks.LOGGER.warn(
                    "Cannot bind phial '{}': unexpected item class {}.",
                    tag,
                    item.getClass().getName()
            );
            return;
        }

        try {
            Field field = AspectEssenceItem.class.getDeclaredField(FIXED_ASPECT_FIELD);
            field.setAccessible(true);
            field.set(phial, aspect);
            GoldenTweaks.LOGGER.debug("Bound aspect '{}' to phial '{}'.", tag, holder.getId());
        } catch (ReflectiveOperationException e) {
            GoldenTweaks.LOGGER.warn(
                    "Failed to reflectively bind aspect '{}' to phial: {}",
                    tag,
                    e.getMessage()
            );
        }
    }
}
