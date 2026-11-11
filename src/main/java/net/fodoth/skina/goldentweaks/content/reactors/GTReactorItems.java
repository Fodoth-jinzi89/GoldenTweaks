package net.fodoth.skina.goldentweaks.content.reactors;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class GTReactorItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    private static final Map<GTReactorSolid, DeferredHolder<Item, Item>> ITEM_BY_SOLID =
            new EnumMap<>(GTReactorSolid.class);

    static {
        for (GTReactorSolid solid : GTReactorSolid.values()) {
            ITEM_BY_SOLID.put(solid, ITEMS.register(solid.id(), () -> new Item(new Item.Properties())));
        }
    }

    private GTReactorItems() {
    }

    public static DeferredHolder<Item, Item> item(GTReactorSolid solid) {
        return ITEM_BY_SOLID.get(solid);
    }

    public static GTReactorSolid solidFor(Item item) {
        for (GTReactorSolid solid : GTReactorSolid.values()) {
            DeferredHolder<Item, Item> holder = ITEM_BY_SOLID.get(solid);
            if (holder != null && holder.get() == item) {
                return solid;
            }
        }
        return null;
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
