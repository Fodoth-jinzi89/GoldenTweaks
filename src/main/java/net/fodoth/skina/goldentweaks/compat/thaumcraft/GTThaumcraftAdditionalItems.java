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
    private static final Map<String, DeferredItem<Item>> WISPS = new LinkedHashMap<>();

    private GTThaumcraftAdditionalItems() {
    }

    /**
     * Registers both a phial ({@code phial_of_essentia_<tag>}) and a wisp essence
     * ({@code wisp_essence_<tag>}) variant for the given aspect tag. The aspect is
     * resolved later from JSON, so items are created with a null fixed aspect and
     * bound reflectively once the aspects are loaded.
     */
    public static void registerAspect(String tag) {
        PHIALS.computeIfAbsent(tag, GTThaumcraftAdditionalItems::createPhial);
        WISPS.computeIfAbsent(tag, GTThaumcraftAdditionalItems::createWisp);
    }

    public static Map<String, DeferredItem<Item>> phials() {
        return Collections.unmodifiableMap(PHIALS);
    }

    public static Map<String, DeferredItem<Item>> wisps() {
        return Collections.unmodifiableMap(WISPS);
    }

    private static DeferredItem<Item> createPhial(String tag) {
        return ITEMS.register(
                "phial_of_essentia_" + tag,
                () -> new AspectEssenceItem(AspectEssenceItem.Kind.PHIAL, null, new Item.Properties())
        );
    }

    private static DeferredItem<Item> createWisp(String tag) {
        return ITEMS.register(
                "wisp_essence_" + tag,
                () -> new AspectEssenceItem(AspectEssenceItem.Kind.WISP, null, new Item.Properties())
        );
    }
}
