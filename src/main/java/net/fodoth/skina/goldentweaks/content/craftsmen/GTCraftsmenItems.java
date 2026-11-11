package net.fodoth.skina.goldentweaks.content.craftsmen;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GTCraftsmenItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredHolder<Item, Item> LDPE_PLATE =
            ITEMS.register("ldpe_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LDPE_ROD =
            ITEMS.register("ldpe_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LDPE_FIBER =
            ITEMS.register("ldpe_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LDPE_BALL =
            ITEMS.register("ldpe_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LDPE_NUGGET =
            ITEMS.register("ldpe_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LLDPE_PLATE =
            ITEMS.register("lldpe_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LLDPE_ROD =
            ITEMS.register("lldpe_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LLDPE_FIBER =
            ITEMS.register("lldpe_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LLDPE_BALL =
            ITEMS.register("lldpe_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LLDPE_NUGGET =
            ITEMS.register("lldpe_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> UHMWPE_PLATE =
            ITEMS.register("uhmwpe_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> UHMWPE_ROD =
            ITEMS.register("uhmwpe_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> UHMWPE_FIBER =
            ITEMS.register("uhmwpe_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> UHMWPE_BALL =
            ITEMS.register("uhmwpe_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> UHMWPE_NUGGET =
            ITEMS.register("uhmwpe_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> IPP_PLATE =
            ITEMS.register("ipp_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> IPP_ROD =
            ITEMS.register("ipp_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> IPP_FIBER =
            ITEMS.register("ipp_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> IPP_BALL =
            ITEMS.register("ipp_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> IPP_NUGGET =
            ITEMS.register("ipp_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPP_PLATE =
            ITEMS.register("spp_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPP_ROD =
            ITEMS.register("spp_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPP_FIBER =
            ITEMS.register("spp_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPP_BALL =
            ITEMS.register("spp_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPP_NUGGET =
            ITEMS.register("spp_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RCPP_PLATE =
            ITEMS.register("rcpp_plate", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RCPP_ROD =
            ITEMS.register("rcpp_rod", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RCPP_FIBER =
            ITEMS.register("rcpp_fiber", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RCPP_BALL =
            ITEMS.register("rcpp_ball", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RCPP_NUGGET =
            ITEMS.register("rcpp_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> HDPE_NUGGET =
            ITEMS.register("hdpe_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> HDPE_FIBER =
            ITEMS.register("hdpe_fiber", () -> new Item(new Item.Properties()));

    private GTCraftsmenItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
