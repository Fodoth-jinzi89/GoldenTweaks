package net.fodoth.skina.goldentweaks.content.reactors;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GTReactorCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoldenTweaks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> REACTORS_TAB =
            CREATIVE_MODE_TABS.register("reactors", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goldentweaks.reactors"))
                    .icon(() -> new ItemStack(GTReactorItems.item(GTReactorSolid.CYCLOPARAPHENYLENE_8).get()))
                    .displayItems((parameters, output) -> {
                        for (GTReactorMaterial material : GTReactorMaterial.values()) {
                            if (material.hasFluid()) {
                                output.accept(GTReactorFluids.bucket(material).get());
                            }
                        }
                        for (GTReactorSolid solid : GTReactorSolid.values()) {
                            output.accept(GTReactorItems.item(solid).get());
                        }
                    })
                    .build());

    private GTReactorCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
