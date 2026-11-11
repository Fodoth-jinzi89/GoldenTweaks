package net.fodoth.skina.goldentweaks.compat.thermal;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 「金子修改：热力幻梦」创造栏：集中展示 Thermal 结晶器专业化组件、整合组件与热力幻梦专属物品。
 */
public final class GTThermalFantasyCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoldenTweaks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THERMAL_FANTASY_TAB =
            CREATIVE_MODE_TABS.register("thermal_fantasy", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goldentweaks.thermal_fantasy"))
                    .icon(() -> new ItemStack(
                            ModList.get().isLoaded("spectrum")
                                    ? ThermalCrystallarieumAugment.CRYSTALLARIEUM_GROWING_AUGMENT.get()
                                    : GTThermalFantasyItems.SPECIALIZATION_UPGRADE_BASE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ThermalAdditionalItems.UPGRADE_AUGMENT_4.get());
                        output.accept(ThermalAdditionalItems.UPGRADE_AUGMENT_5.get());
                        output.accept(ThermalAdditionalItems.UPGRADE_AUGMENT_6.get());
                        if (ModList.get().isLoaded("spectrum")) {
                            output.accept(ThermalCrystallarieumAugment.CRYSTALLARIEUM_GROWING_AUGMENT.get());
                        }
                        output.accept(GTThermalFantasyItems.TRAY_DISTILLATION_EXTRACTION_AUGMENT.get());
                        output.accept(GTThermalFantasyItems.OIL_DISTILLATION_AUGMENT.get());
                        output.accept(GTThermalFantasyItems.AIR_DISTILLATION_AUGMENT.get());
                        output.accept(GTThermalFantasyItems.RARE_METAL_CRYSTALLIZATION_AUGMENT.get());
                        output.accept(GTThermalFantasyItems.LAVA_PRODUCTION_AUGMENT.get());
                        output.accept(GTThermalFantasyItems.SPECIALIZATION_UPGRADE_BASE.get());
                    })
                    .build());

    private GTThermalFantasyCreativeTab() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
