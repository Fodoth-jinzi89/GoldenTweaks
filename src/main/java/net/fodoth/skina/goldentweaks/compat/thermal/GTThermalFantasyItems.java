package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.core.util.helpers.AugmentDataHelper;
import cofh.lib.util.Utils;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 「金子修改：热力幻梦」创造栏专属物品。
 *
 * <p>「专业化升级基板」是制作热力专业化组件的通用底材；「组件：塔板分馏提取」是流体精炼机的专业化组件。
 * 物品沿用 goldentweaks 命名空间，模型/纹理/lang 见 assets/goldentweaks。</p>
 */
public final class GTThermalFantasyItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredItem<Item> SPECIALIZATION_UPGRADE_BASE = ITEMS.register(
            "specialization_upgrade_base",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredItem<Item> TRAY_DISTILLATION_EXTRACTION_AUGMENT = ITEMS.register(
            "tray_distillation_extraction_augment",
            () -> new DistillationAugmentItem(
                    Utils.itemProperties(),
                    AugmentDataHelper.builder()
                            .type("Machine")
                            .mod("Distillation", 1.0F)
                            .build()
            )
    );

    public static final DeferredItem<Item> OIL_DISTILLATION_AUGMENT = ITEMS.register(
            "oil_distillation_augment",
            () -> new OilDistillationAugmentItem(
                    Utils.itemProperties(),
                    AugmentDataHelper.builder()
                            .type("Machine")
                            .mod("OilDistillation", 1.0F)
                            .build()
            )
    );

    public static final DeferredItem<Item> AIR_DISTILLATION_AUGMENT = ITEMS.register(
            "air_distillation_augment",
            () -> new AirDistillationAugmentItem(
                    Utils.itemProperties(),
                    AugmentDataHelper.builder()
                            .type("Machine")
                            .mod("AirDistillation", 1.0F)
                            .build()
            )
    );

    public static final DeferredItem<Item> RARE_METAL_CRYSTALLIZATION_AUGMENT = ITEMS.register(
            "rare_metal_crystallization_augment",
            () -> new RareMetalCrystallizationAugmentItem(
                    Utils.itemProperties(),
                    AugmentDataHelper.builder()
                            .type("Machine")
                            .mod("RareMetalCrystallization", 1.0F)
                            .build()
            )
    );

    public static final DeferredItem<Item> LAVA_PRODUCTION_AUGMENT = ITEMS.register(
            "lava_production_augment",
            () -> new LavaProductionAugmentItem(
                    Utils.itemProperties(),
                    AugmentDataHelper.builder()
                            .type("Machine")
                            .mod("LavaProduction", 1.0F)
                            .build()
            )
    );

    private GTThermalFantasyItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
