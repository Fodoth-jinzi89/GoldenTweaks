package net.fodoth.skina.goldentweaks.compat.questshop;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class QSAdditionalItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredItem<Item> RADIANT_GOLD =
            ITEMS.register("radiant_gold",
                    () -> new RadiantGoldItem(new Item.Properties()));

    public static final DeferredItem<Item> BRILLIANT_GOLD =
            ITEMS.register("brilliant_gold",
                    () -> new BrilliantGoldItem(new Item.Properties()));

    public static final DeferredItem<Item> GLORIOUS_GOLD =
            ITEMS.register("glorious_gold",
                    () -> new GloriousGoldItem(new Item.Properties()));

    public static final DeferredItem<Item> LUMINOUS_GOLD =
            ITEMS.register("luminous_gold",
                    () -> new LuminousGoldItem(new Item.Properties()));

    public static final DeferredItem<Item> IMMACULATE_GOLD =
            ITEMS.register("immaculate_gold",
                    () -> new ImmaculateGoldItem(new Item.Properties()));

    private QSAdditionalItems() {
    }
}