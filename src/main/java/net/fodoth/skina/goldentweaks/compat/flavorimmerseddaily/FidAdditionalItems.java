package net.fodoth.skina.goldentweaks.compat.flavorimmerseddaily;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * flavor_immersed_daily 自 2026.9.26 起删除了茶壶盖（teapotcover），但 fidworkblock 的茶壶
 * （TeapotBlock.COVER_ID）与 mixin/feature/flavorimmerseddaily/TeapotBlockMixin 仍按
 * flavor_immersed_daily:teapotcover 判定加盖与掉落，导致茶壶无法密封、盖子也无从获取。
 * 这里按原命名空间补注册回该物品，配套资源见 assets|data/flavor_immersed_daily。
 */
public class FidAdditionalItems {

    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems("flavor_immersed_daily");

    public static final DeferredItem<Item> TEAPOT_COVER =
            ITEMS.register("teapotcover", () -> new Item(new Item.Properties()));

    /** 原物品所在的创造栏：烟火凡人心·工具（对应模组 ModCreateTab 的 fi_dtools）。 */
    private static final ResourceLocation DTOOLS_TAB =
            ResourceLocation.fromNamespaceAndPath("flavor_immersed_daily", "fi_dtools");

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(DTOOLS_TAB)) {
            event.accept(TEAPOT_COVER.get());
        }
    }

    private FidAdditionalItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        // BuildCreativeModeTabContentsEvent 是 IModBusEvent，只能在模组事件总线上监听
        eventBus.register(FidAdditionalItems.class);
    }
}
