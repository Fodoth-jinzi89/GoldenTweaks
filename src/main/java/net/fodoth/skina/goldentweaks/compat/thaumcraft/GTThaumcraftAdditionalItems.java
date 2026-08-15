package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

    /**
     * Invisible rendering proxy whose item model carries the cosmic icon of the
     * {@code cosmic} aspect. Only used to draw the aspect icon with the
     * renderblender cosmic renderer in GUIs; it is not added to any creative tab
     * and has no recipes.
     */
    public static final DeferredItem<Item> ASPECT_ICON_COSMIC =
            ITEMS.register("aspect_icon_cosmic", () -> new Item(new Item.Properties()));

    private static final Map<String, DeferredItem<Item>> PHIALS = new LinkedHashMap<>();
    private static final Map<String, DeferredItem<Item>> WISPS = new LinkedHashMap<>();

    private static ItemStack cosmicIconStack;

    private GTThaumcraftAdditionalItems() {
    }

    /** Lazy {@link ItemStack} of the cosmic {@code cosmic} aspect icon proxy item. */
    public static ItemStack cosmicIconStack() {
        if (cosmicIconStack == null) {
            cosmicIconStack = new ItemStack(ASPECT_ICON_COSMIC.get());
        }
        return cosmicIconStack;
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
