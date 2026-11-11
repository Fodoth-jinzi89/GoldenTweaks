package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.core.util.helpers.AugmentDataHelper;
import cofh.lib.util.Utils;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Thermal 结晶器（{@code thermal:machine_crystallizer}）的「专业化升级」：光谱墨法结晶。
 *
 * <p>参考 1.12.2 Thermal Expansion 的 Specialization 组件——装上后改变机器可用的配方集合。这里把光谱
 * （Spectrum）的 {@code spectrum:crystallarieum_growing} 配方也纳入结晶器，并把墨水消耗整个去掉；装上本组件
 * 才会生效（见 {@code MachineCrystallizerSpectrumMixin} 与 {@link SpectrumCrystallizerRecipe}）。</p>
 *
 * <p>物品使用 goldentweaks 命名空间；模型/纹理/lang 见 assets/goldentweaks，
 * 并集中展示在「金子修改：热力幻梦」创造栏（见 {@link GTThermalFantasyCreativeTab}）。</p>
 */
public final class ThermalCrystallarieumAugment {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredItem<Item> CRYSTALLARIEUM_GROWING_AUGMENT = ITEMS.register(
            "crystallarieum_growing_augment",
            () -> new CrystallarieumAugmentItem(
                    Utils.itemProperties(),
                    AugmentDataHelper.builder()
                            .type("Machine")
                            .mod("Crystallarieum", 1.0F)
                            .build()
            )
    );

    private ThermalCrystallarieumAugment() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    /** 本组件是否为该机器上唯一生效的专业化升级（升级槽位里最先出现的那个）。 */
    public static boolean isActive(List<ItemStack> augments) {
        return ThermalSpecializations.isActive(augments, CRYSTALLARIEUM_GROWING_AUGMENT.get());
    }
}
