package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GTThaumcraftAdditionalTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoldenTweaks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THAUMCRAFT_TAB =
            TABS.register("thaumcraft", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goldentweaks.thaumcraft"))
                    .icon(() -> new ItemStack(GTThaumcraftAdditionalBlocks.INFUSION_INTERCEPTER_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(GTThaumcraftAdditionalBlocks.INFUSION_INTERCEPTER_ITEM.get());
                        output.accept(GTThaumcraftAdditionalItems.WARPTHEORY_CLEANSER.get());
                        GTThaumcraftAdditionalItems.phials().values()
                                .forEach(phial -> output.accept(phial.get()));
                        GTThaumcraftAdditionalItems.wisps().values()
                                .forEach(wisp -> output.accept(wisp.get()));
                    })
                    .build()
            );

    private GTThaumcraftAdditionalTabs() {
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
