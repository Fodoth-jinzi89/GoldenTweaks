package net.fodoth.skina.goldentweaks.compat.alshanex_familiars;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.registry.alshanex_familiars.AFAdditionalItemsRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AFAdditionalCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoldenTweaks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FAMILIARS_TAB =
            CREATIVE_MODE_TABS.register("familiars", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goldentweaks.familiars"))
                    .icon(() -> new ItemStack(
                            AFAdditionalItemsRegistry.ARMOR_PLATE_TIER_10.get()
                    ))
                    .displayItems((parameters, output) -> {

                        output.accept(AFAdditionalItemsRegistry.ARMOR_PLATE_TIER_4.get());
                        output.accept(AFAdditionalItemsRegistry.ARMOR_PLATE_TIER_5.get());
                        output.accept(AFAdditionalItemsRegistry.ARMOR_PLATE_TIER_6.get());
                        output.accept(AFAdditionalItemsRegistry.ARMOR_PLATE_TIER_7.get());
                        output.accept(AFAdditionalItemsRegistry.ARMOR_PLATE_TIER_8.get());
                        output.accept(AFAdditionalItemsRegistry.ARMOR_PLATE_TIER_9.get());
                        output.accept(AFAdditionalItemsRegistry.ARMOR_PLATE_TIER_10.get());

                    })
                    .build()
            );

    private AFAdditionalCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
