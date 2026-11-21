package net.fodoth.skina.goldentweaks.content;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GoldenDandelion {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GoldenTweaks.MODID);
    public static final DeferredItem<Item> ITEM = ITEMS.registerSimpleItem("golden_dandelion");

    private GoldenDandelion() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(GoldenDandelion::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) event.accept(ITEM);
    }
}
