package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.common.items.AspectEssenceItem;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GTThaumcraftAdditionalItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredItem<Item> WARPTHEORY_CLEANSER =
            ITEMS.register("warptheory_cleanser", GTCleanserItem::new);

    private static final Map<String, DeferredItem<Item>> PHIALS = new LinkedHashMap<>();

    private GTThaumcraftAdditionalItems() {
    }

    /**
     * Registers a fixed phial variant {@code phial_of_essentia_<tag>}. The aspect
     * itself is resolved later from JSON, so the item is created with a null
     * fixed aspect and bound reflectively once the aspects are loaded.
     */
    public static DeferredItem<Item> registerPhial(String tag) {
        return PHIALS.computeIfAbsent(tag, GTThaumcraftAdditionalItems::createPhial);
    }

    public static Map<String, DeferredItem<Item>> phials() {
        return Collections.unmodifiableMap(PHIALS);
    }

    private static DeferredItem<Item> createPhial(String tag) {
        return ITEMS.register(
                "phial_of_essentia_" + tag,
                () -> new AspectEssenceItem(AspectEssenceItem.Kind.PHIAL, null, new Item.Properties())
        );
    }
}
