package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.api.aspects.Aspect;

import java.util.Collection;

/**
 * Tints the custom phial variants by their aspect color. The vanilla
 * {@code TCClientColors} only covers the {@code thaumcraft:} namespace, so the
 * {@code goldentweaks:} phials replicate the same path-based aspect lookup.
 */
public final class GTAspectPhialColors {

    private static final String PHIAL_PREFIX = "phial_of_essentia_";

    private GTAspectPhialColors() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterColorHandlersEvent.Item.class, GTAspectPhialColors::onItemColors);
    }

    private static void onItemColors(RegisterColorHandlersEvent.Item event) {
        Collection<DeferredItem<Item>> phials = GTThaumcraftAdditionalItems.phials().values();
        if (phials.isEmpty()) {
            return;
        }
        ItemLike[] items = phials.stream()
                .map(DeferredItem::get)
                .toArray(ItemLike[]::new);
        event.register(GTAspectPhialColors::essenceTint, items);
    }

    private static int essenceTint(ItemStack stack, int layer) {
        if (layer == 0) {
            // layer0 is the glass vial; leave it untinted.
            return -1;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id.getPath();
        if (!path.startsWith(PHIAL_PREFIX)) {
            return -1;
        }

        Aspect aspect = Aspect.get(path.substring(PHIAL_PREFIX.length()));
        if (aspect == null) {
            return -1;
        }
        return 0xFF000000 | (aspect.color() & 0xFFFFFF);
    }
}
