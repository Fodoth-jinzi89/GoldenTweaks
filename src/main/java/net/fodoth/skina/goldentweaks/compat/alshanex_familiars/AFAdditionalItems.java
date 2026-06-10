package net.fodoth.skina.goldentweaks.compat.alshanex_familiars;

import net.alshanex.familiarslib.registry.ComponentRegistry;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableComponent;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableSystem;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AFAdditionalItems {

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_1;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_2;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_3;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_4;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_5;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_6;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_7;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_8;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_9;
    public static final DeferredHolder<Item, Item> ARMOR_PLATE_TIER_10;
    public static final DeferredHolder<Item, Item> MAGIC_POWER_TIER_1;
    public static final DeferredHolder<Item, Item> MAGIC_POWER_TIER_2;
    public static final DeferredHolder<Item, Item> MAGIC_POWER_TIER_3;
    public static final DeferredHolder<Item, Item> MAGIC_POWER_TIER_4;
    public static final DeferredHolder<Item, Item> MAGIC_POWER_TIER_5;
    public static final DeferredHolder<Item, Item> MAGIC_POWER_TIER_6;
    public static final DeferredHolder<Item, Item> MAGIC_POWER_TIER_7;
    public static final DeferredHolder<Item, Item> MAGIC_POWER_TIER_8;
    public static final DeferredHolder<Item, Item> MAGIC_POWER_TIER_9;
    public static final DeferredHolder<Item, Item> MAGIC_POWER_TIER_10;
    public static final DeferredHolder<Item, Item> MAGIC_RESIST_TIER_1;
    public static final DeferredHolder<Item, Item> MAGIC_RESIST_TIER_2;
    public static final DeferredHolder<Item, Item> MAGIC_RESIST_TIER_3;
    public static final DeferredHolder<Item, Item> MAGIC_RESIST_TIER_4;
    public static final DeferredHolder<Item, Item> MAGIC_RESIST_TIER_5;
    public static final DeferredHolder<Item, Item> MAGIC_RESIST_TIER_6;
    public static final DeferredHolder<Item, Item> MAGIC_RESIST_TIER_7;
    public static final DeferredHolder<Item, Item> MAGIC_RESIST_TIER_8;
    public static final DeferredHolder<Item, Item> MAGIC_RESIST_TIER_9;
    public static final DeferredHolder<Item, Item> MAGIC_RESIST_TIER_10;
    public static final DeferredHolder<Item, Item> LIFE_FRUIT_TIER_1;
    public static final DeferredHolder<Item, Item> LIFE_FRUIT_TIER_2;
    public static final DeferredHolder<Item, Item> LIFE_FRUIT_TIER_3;
    public static final DeferredHolder<Item, Item> LIFE_FRUIT_TIER_4;
    public static final DeferredHolder<Item, Item> LIFE_FRUIT_TIER_5;
    public static final DeferredHolder<Item, Item> LIFE_FRUIT_TIER_6;
    public static final DeferredHolder<Item, Item> LIFE_FRUIT_TIER_7;
    public static final DeferredHolder<Item, Item> LIFE_FRUIT_TIER_8;
    public static final DeferredHolder<Item, Item> LIFE_FRUIT_TIER_9;
    public static final DeferredHolder<Item, Item> LIFE_FRUIT_TIER_10;
    public static final DeferredHolder<Item, Item> INVERTED_FAMILIAR_SPELLBOOK;

    private AFAdditionalItems() {
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

        ARMOR_PLATE_TIER_1 = consumableItem(
                "armor_plate_tier_1",
                FamiliarConsumableSystem.ConsumableType.ARMOR,
                1,
                Rarity.COMMON
        );

        ARMOR_PLATE_TIER_2 = consumableItem(
                "armor_plate_tier_2",
                FamiliarConsumableSystem.ConsumableType.ARMOR,
                2,
                Rarity.UNCOMMON
        );

        ARMOR_PLATE_TIER_3 = consumableItem(
                "armor_plate_tier_3",
                FamiliarConsumableSystem.ConsumableType.ARMOR,
                3,
                Rarity.RARE
        );

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

        MAGIC_POWER_TIER_1 = consumableItem(
                "magic_power_tier_1",
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER,
                1,
                Rarity.COMMON
        );

        MAGIC_POWER_TIER_2 = consumableItem(
                "magic_power_tier_2",
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER,
                2,
                Rarity.UNCOMMON
        );

        MAGIC_POWER_TIER_3 = consumableItem(
                "magic_power_tier_3",
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER,
                3,
                Rarity.RARE
        );

        MAGIC_POWER_TIER_4 = consumableItem(
                "magic_power_tier_4",
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER,
                4,
                Rarity.UNCOMMON
        );

        MAGIC_POWER_TIER_5 = consumableItem(
                "magic_power_tier_5",
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER,
                5,
                Rarity.RARE
        );

        MAGIC_POWER_TIER_6 = consumableItem(
                "magic_power_tier_6",
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER,
                6,
                Rarity.RARE
        );

        MAGIC_POWER_TIER_7 = consumableItem(
                "magic_power_tier_7",
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER,
                7,
                Rarity.EPIC
        );

        MAGIC_POWER_TIER_8 = consumableItem(
                "magic_power_tier_8",
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER,
                8,
                Rarity.EPIC
        );

        MAGIC_POWER_TIER_9 = consumableItem(
                "magic_power_tier_9",
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER,
                9,
                Rarity.EPIC
        );

        MAGIC_POWER_TIER_10 = consumableItem(
                "magic_power_tier_10",
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER,
                10,
                Rarity.EPIC
        );

        MAGIC_RESIST_TIER_1 = consumableItem(
                "magic_resist_tier_1",
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST,
                1,
                Rarity.COMMON
        );

        MAGIC_RESIST_TIER_2 = consumableItem(
                "magic_resist_tier_2",
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST,
                2,
                Rarity.UNCOMMON
        );

        MAGIC_RESIST_TIER_3 = consumableItem(
                "magic_resist_tier_3",
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST,
                3,
                Rarity.RARE
        );

        MAGIC_RESIST_TIER_4 = consumableItem(
                "magic_resist_tier_4",
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST,
                4,
                Rarity.UNCOMMON
        );

        MAGIC_RESIST_TIER_5 = consumableItem(
                "magic_resist_tier_5",
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST,
                5,
                Rarity.RARE
        );

        MAGIC_RESIST_TIER_6 = consumableItem(
                "magic_resist_tier_6",
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST,
                6,
                Rarity.RARE
        );

        MAGIC_RESIST_TIER_7 = consumableItem(
                "magic_resist_tier_7",
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST,
                7,
                Rarity.EPIC
        );

        MAGIC_RESIST_TIER_8 = consumableItem(
                "magic_resist_tier_8",
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST,
                8,
                Rarity.EPIC
        );

        MAGIC_RESIST_TIER_9 = consumableItem(
                "magic_resist_tier_9",
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST,
                9,
                Rarity.EPIC
        );

        MAGIC_RESIST_TIER_10 = consumableItem(
                "magic_resist_tier_10",
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST,
                10,
                Rarity.EPIC
        );

        LIFE_FRUIT_TIER_1 = consumableItem(
                "life_fruit_tier_1",
                FamiliarConsumableSystem.ConsumableType.HEALTH,
                1,
                Rarity.COMMON
        );

        LIFE_FRUIT_TIER_2 = consumableItem(
                "life_fruit_tier_2",
                FamiliarConsumableSystem.ConsumableType.HEALTH,
                2,
                Rarity.UNCOMMON
        );

        LIFE_FRUIT_TIER_3 = consumableItem(
                "life_fruit_tier_3",
                FamiliarConsumableSystem.ConsumableType.HEALTH,
                3,
                Rarity.RARE
        );

        LIFE_FRUIT_TIER_4 = consumableItem(
                "life_fruit_tier_4",
                FamiliarConsumableSystem.ConsumableType.HEALTH,
                4,
                Rarity.UNCOMMON
        );

        LIFE_FRUIT_TIER_5 = consumableItem(
                "life_fruit_tier_5",
                FamiliarConsumableSystem.ConsumableType.HEALTH,
                5,
                Rarity.RARE
        );

        LIFE_FRUIT_TIER_6 = consumableItem(
                "life_fruit_tier_6",
                FamiliarConsumableSystem.ConsumableType.HEALTH,
                6,
                Rarity.RARE
        );

        LIFE_FRUIT_TIER_7 = consumableItem(
                "life_fruit_tier_7",
                FamiliarConsumableSystem.ConsumableType.HEALTH,
                7,
                Rarity.EPIC
        );

        LIFE_FRUIT_TIER_8 = consumableItem(
                "life_fruit_tier_8",
                FamiliarConsumableSystem.ConsumableType.HEALTH,
                8,
                Rarity.EPIC
        );

        LIFE_FRUIT_TIER_9 = consumableItem(
                "life_fruit_tier_9",
                FamiliarConsumableSystem.ConsumableType.HEALTH,
                9,
                Rarity.EPIC
        );

        LIFE_FRUIT_TIER_10 = consumableItem(
                "life_fruit_tier_10",
                FamiliarConsumableSystem.ConsumableType.HEALTH,
                10,
                Rarity.EPIC
        );

        INVERTED_FAMILIAR_SPELLBOOK = ITEMS.register(
                "inverted_familiar_spellbook",
                InvertedFamiliarSpellbookItem::new
        );
    }
}
