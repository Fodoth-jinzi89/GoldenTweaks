package net.fodoth.skina.goldentweaks.compat.createoreexcavation;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 与 Create Ore Excavation 联动的 GoldenTweaks 物品。
 *
 * <p>这里用普通 {@link DeferredRegister} 而不是 Create 的 Registrate：整合包里的 Registrate（1.4.8）
 * 会自己再往创造栏里塞一份条目，和下面这个监听器叠加时会抛
 * 「Itemstack already exists in the tab's list」把服务端带崩。物品的模型与语言文件都是随包提供的，
 * 走 DeferredRegister 不损失任何东西。</p>
 */
public class GTCreateOreExcavationItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredItem<GoldenVeinFinderItem> GOLDEN_VEIN_FINDER =
            ITEMS.register("golden_vein_finder",
                    () -> new GoldenVeinFinderItem(new Item.Properties().stacksTo(1)));

    /** 物品放进 Create Ore Excavation 自己的创造栏。 */
    private static final ResourceLocation COE_TAB =
            ResourceLocation.fromNamespaceAndPath("createoreexcavation", "create_ore_excavation");

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {

        if (!event.getTabKey().location().equals(COE_TAB)) {
            return;
        }
        // 已经有别处（例如 Registrate）塞过同一份条目时不再重复添加，否则 NeoForge 会直接抛异常
        ItemStack stack = new ItemStack(GOLDEN_VEIN_FINDER.get());
        if (!event.getParentEntries().contains(stack)) {
            event.accept(stack);
        }
    }

    private GTCreateOreExcavationItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        // BuildCreativeModeTabContentsEvent 是 IModBusEvent，只能在模组事件总线上监听
        modEventBus.register(GTCreateOreExcavationItems.class);
    }
}
