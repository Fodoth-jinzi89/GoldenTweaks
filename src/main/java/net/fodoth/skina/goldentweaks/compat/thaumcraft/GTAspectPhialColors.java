package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import thaumcraft.api.aspects.Aspect;

import java.util.ArrayList;
import java.util.List;

/**
 * Tints the custom phial and wisp-essence variants by their aspect color. The
 * vanilla {@code TCClientColors} only covers the {@code thaumcraft:} namespace,
 * so the {@code goldentweaks:} variants replicate the same path-based lookup.
 */
public final class GTAspectPhialColors {

    private static final String PHIAL_PREFIX = "phial_of_essentia_";
    private static final String WISP_PREFIX = "wisp_essence_";

    private GTAspectPhialColors() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterColorHandlersEvent.Item.class, GTAspectPhialColors::onItemColors);
    }

    private static void onItemColors(RegisterColorHandlersEvent.Item event) {
        List<ItemLike> items = new ArrayList<>();
        GTThaumcraftAdditionalItems.phials().values().forEach(h -> items.add(h.get()));
        GTThaumcraftAdditionalItems.wisps().values().forEach(h -> items.add(h.get()));
        if (items.isEmpty()) {
            return;
        }
        event.register(GTAspectPhialColors::essenceTint, items.toArray(ItemLike[]::new));
    }

    private static int essenceTint(ItemStack stack, int layer) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return -1;
        }
        String path = id.getPath();

        // A phial's layer0 is the glass vial; leave it untinted.
        if (path.startsWith("phial_of_essentia") && layer == 0) {
            return -1;
        }

        Aspect aspect = aspectFromPath(path);
        if (aspect == null) {
            return -1;
        }
        return 0xFF000000 | (aspect.color() & 0xFFFFFF);
    }

    private static Aspect aspectFromPath(String path) {
        if (path.startsWith(PHIAL_PREFIX)) {
            return Aspect.get(path.substring(PHIAL_PREFIX.length()));
        }
        if (path.startsWith(WISP_PREFIX)) {
            return Aspect.get(path.substring(WISP_PREFIX.length()));
        }
        return null;
    }
}
