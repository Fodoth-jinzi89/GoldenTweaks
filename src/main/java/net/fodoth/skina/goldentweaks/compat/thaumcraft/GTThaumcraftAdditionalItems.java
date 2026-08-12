package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GTThaumcraftAdditionalItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredItem<Item> WARPTHEORY_CLEANSER =
            ITEMS.register("warptheory_cleanser", GTCleanserItem::new);

    private GTThaumcraftAdditionalItems() {
    }
}
