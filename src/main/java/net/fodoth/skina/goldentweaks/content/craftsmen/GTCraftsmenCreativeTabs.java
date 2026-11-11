package net.fodoth.skina.goldentweaks.content.craftsmen;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GTCraftsmenCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoldenTweaks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CRAFTSMEN_TAB =
            TABS.register("craftsmen", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goldentweaks.craftsmen"))
                    .icon(() -> new ItemStack(GTCraftsmenItems.LDPE_PLATE.get()))
                    .displayItems((parameters, output) -> {
                        if (ModList.get().isLoaded("mekanism")) {
                            acceptExternal(output, "mekanism:hdpe_sheet");
                            acceptExternal(output, "mekanism:hdpe_stick");
                            output.accept(GTCraftsmenItems.HDPE_FIBER.get());
                            acceptExternal(output, "mekanism:hdpe_pellet");
                            output.accept(GTCraftsmenItems.HDPE_NUGGET.get());
                        }
                        output.accept(GTCraftsmenItems.LDPE_PLATE.get());
                        output.accept(GTCraftsmenItems.LDPE_ROD.get());
                        output.accept(GTCraftsmenItems.LDPE_FIBER.get());
                        output.accept(GTCraftsmenItems.LDPE_BALL.get());
                        output.accept(GTCraftsmenItems.LDPE_NUGGET.get());
                        output.accept(GTCraftsmenItems.LLDPE_PLATE.get());
                        output.accept(GTCraftsmenItems.LLDPE_ROD.get());
                        output.accept(GTCraftsmenItems.LLDPE_FIBER.get());
                        output.accept(GTCraftsmenItems.LLDPE_BALL.get());
                        output.accept(GTCraftsmenItems.LLDPE_NUGGET.get());
                        output.accept(GTCraftsmenItems.UHMWPE_PLATE.get());
                        output.accept(GTCraftsmenItems.UHMWPE_ROD.get());
                        output.accept(GTCraftsmenItems.UHMWPE_FIBER.get());
                        output.accept(GTCraftsmenItems.UHMWPE_BALL.get());
                        output.accept(GTCraftsmenItems.UHMWPE_NUGGET.get());
                        output.accept(GTCraftsmenItems.IPP_PLATE.get());
                        output.accept(GTCraftsmenItems.IPP_ROD.get());
                        output.accept(GTCraftsmenItems.IPP_FIBER.get());
                        output.accept(GTCraftsmenItems.IPP_BALL.get());
                        output.accept(GTCraftsmenItems.IPP_NUGGET.get());
                        output.accept(GTCraftsmenItems.SPP_PLATE.get());
                        output.accept(GTCraftsmenItems.SPP_ROD.get());
                        output.accept(GTCraftsmenItems.SPP_FIBER.get());
                        output.accept(GTCraftsmenItems.SPP_BALL.get());
                        output.accept(GTCraftsmenItems.SPP_NUGGET.get());
                        output.accept(GTCraftsmenItems.RCPP_PLATE.get());
                        output.accept(GTCraftsmenItems.RCPP_ROD.get());
                        output.accept(GTCraftsmenItems.RCPP_FIBER.get());
                        output.accept(GTCraftsmenItems.RCPP_BALL.get());
                        output.accept(GTCraftsmenItems.RCPP_NUGGET.get());
                    })
                    .build());

    private GTCraftsmenCreativeTabs() {
    }

    private static void acceptExternal(CreativeModeTab.Output output, String id) {
        BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id))
                .ifPresent(item -> output.accept(item));
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
