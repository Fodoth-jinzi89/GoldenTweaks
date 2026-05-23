package net.fodoth.skina.goldentweaks.registry.alshanex_familiars;

import net.alshanex.familiarslib.registry.ComponentRegistry;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableComponent;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableSystem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AFAdditionalItemsRegistry {

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.createItems("goldentweaks");

    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_4;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_5;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_6;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_7;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_8;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_9;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_10;

    private AFAdditionalItemsRegistry() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static DeferredHolder<Item, Item> consumableItem(
            String name,
            FamiliarConsumableSystem.ConsumableType type,
            int tier,
            Rarity rarity
    ) {

        return ITEMS.register(name, () -> new Item(
                new Item.Properties()
                        .stacksTo(64)
                        .rarity(rarity)
                        .component(
                                ComponentRegistry.FAMILIAR_CONSUMABLE.get(),
                                new FamiliarConsumableComponent(type, tier)
                        )
        ));
    }

    static {

        ARMOR_PLATE_TIER_4 = consumableItem(
                "armor_plate_tier_4",
                FamiliarConsumableSystem.ConsumableType.ARMOR,
                4,
                Rarity.UNCOMMON
        );

        ARMOR_PLATE_TIER_5 = consumableItem(
                "armor_plate_tier_5",
                FamiliarConsumableSystem.ConsumableType.ARMOR,
                5,
                Rarity.RARE
        );

        ARMOR_PLATE_TIER_6 = consumableItem(
                "armor_plate_tier_6",
                FamiliarConsumableSystem.ConsumableType.ARMOR,
                6,
                Rarity.RARE
        );

        ARMOR_PLATE_TIER_7 = consumableItem(
                "armor_plate_tier_7",
                FamiliarConsumableSystem.ConsumableType.ARMOR,
                7,
                Rarity.EPIC
        );

        ARMOR_PLATE_TIER_8 = consumableItem(
                "armor_plate_tier_8",
                FamiliarConsumableSystem.ConsumableType.ARMOR,
                8,
                Rarity.EPIC
        );

        ARMOR_PLATE_TIER_9 = consumableItem(
                "armor_plate_tier_9",
                FamiliarConsumableSystem.ConsumableType.ARMOR,
                9,
                Rarity.EPIC
        );

        ARMOR_PLATE_TIER_10 = consumableItem(
                "armor_plate_tier_10",
                FamiliarConsumableSystem.ConsumableType.ARMOR,
                10,
                Rarity.EPIC
        );
    }
}