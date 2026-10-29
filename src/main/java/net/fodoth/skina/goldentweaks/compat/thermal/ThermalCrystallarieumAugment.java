package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.core.util.helpers.AugmentDataHelper;
import cofh.lib.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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
 * <p>物品沿用 thermal 命名空间，与 1~3 级升级组件一致；模型/纹理/lang 见 assets/thermal。</p>
 */
public final class ThermalCrystallarieumAugment {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("thermal");

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

    /** 升级组件所在的创造栏：thermal 物品栏。CoFH 把该栏注册为 "thermal.items"，故栏位 id 是 thermal:thermal.items。 */
    private static final String ITEMS_TAB_NAMESPACE = "thermal";
    private static final String ITEMS_TAB_PATH_SUFFIX = "items";

    private ThermalCrystallarieumAugment() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        eventBus.register(ThermalCrystallarieumAugment.class);
    }

    /** 给定机器上已安装的升级组件，判断本组件是否在其中。 */
    public static boolean isInstalled(List<ItemStack> augments) {
        Item augment = CRYSTALLARIEUM_GROWING_AUGMENT.get();
        for (ItemStack stack : augments) {
            if (stack.is(augment)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        ResourceLocation key = event.getTabKey().location();
        if (key.getNamespace().equals(ITEMS_TAB_NAMESPACE) && key.getPath().endsWith(ITEMS_TAB_PATH_SUFFIX)) {
            event.accept(CRYSTALLARIEUM_GROWING_AUGMENT.get());
        }
    }
}
