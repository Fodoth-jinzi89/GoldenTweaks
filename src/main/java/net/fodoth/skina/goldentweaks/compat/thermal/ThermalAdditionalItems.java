package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.core.util.helpers.AugmentDataHelper;
import cofh.lib.util.Utils;
import cofh.thermal.lib.common.item.AugmentItem;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Thermal 的「整合组件」(thermal:upgrade_augment) 原本只有 1~3 级，BaseMod 依次为 2 / 3 / 4，
 * 作用是把机器的基础属性按该倍率放大（其它升级组件据此收益更多）。
 * 这里按同样方式补上 4~6 级，倍率取上一级的 4 倍：16 / 64 / 256。
 * 物品使用 goldentweaks 命名空间，模型/纹理/lang 见 assets/goldentweaks，
 * 并集中展示在「金子修改：热力幻梦」创造栏（见 {@link GTThermalFantasyCreativeTab}）。
 */
public class ThermalAdditionalItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredItem<Item> UPGRADE_AUGMENT_4 = upgradeAugment("upgrade_augment_4", 16.0F);
    public static final DeferredItem<Item> UPGRADE_AUGMENT_5 = upgradeAugment("upgrade_augment_5", 64.0F);
    public static final DeferredItem<Item> UPGRADE_AUGMENT_6 = upgradeAugment("upgrade_augment_6", 256.0F);

    private ThermalAdditionalItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static DeferredItem<Item> upgradeAugment(String name, float baseMod) {
        return ITEMS.register(name, () -> new AugmentItem(
                Utils.itemProperties(),
                AugmentDataHelper.builder()
                        .type("Upgrade")
                        .mod("BaseMod", baseMod)
                        .build()
        ));
    }
}
