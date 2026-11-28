package net.fodoth.skina.goldentweaks.content.multiblock.common;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creative tab holding every multiblock piece: the components, the shell and the host.
 *
 * <p>The projector doubles as the tab icon, since it is the tool the player reaches for first
 * when building one of these machines.</p>
 */
public final class GTMultiblockCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoldenTweaks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MULTIBLOCK_TAB =
            TABS.register("multiblock", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goldentweaks.multiblock"))
                    .icon(() -> new ItemStack(GTMultiblockBlocks.PROJECTOR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(GTMultiblockBlocks.PROJECTOR.get());
                        output.accept(GTMultiblockBlocks.SHELL_PE_ITEM.get());
                        output.accept(GTMultiblockBlocks.SHELL_PP_ITEM.get());
                        output.accept(GTMultiblockBlocks.SHELL_ALUMINUM_ITEM.get());
                        // The host is registered by whichever path is active, so it is resolved by
                        // name instead of through a field that only one of the two paths holds.
                        acceptExternal(output, "goldentweaks:chemical_plant_controller");
                        acceptExternal(output, "goldentweaks:advanced_chemical_plant_controller");
                        acceptExternal(output, "goldentweaks:vacuum_cooler_controller");
                        // Ports only exist with avaritia_more_items installed.
                        GTMultiblockBlocks.PORT_ITEMS.values().stream()
                                .flatMap(items -> items.values().stream())
                                .flatMap(tiers -> tiers.values().stream())
                                .forEach(item -> output.accept(item.get()));
                        output.accept(GTMultiblockBlocks.CONTROL_CIRCUIT_BASE.get());
                        output.accept(GTMultiblockBlocks.CONTROL_CIRCUIT_OVERCLOCK_4_2.get());
                        output.accept(GTMultiblockBlocks.CONTROL_CIRCUIT_PARALLEL_1.get());
                        output.accept(GTMultiblockBlocks.CONTROL_CIRCUIT_PARALLEL_4X.get());
                    })
                    .build());

    private GTMultiblockCreativeTabs() {
    }

    private static void acceptExternal(CreativeModeTab.Output output, String id) {
        BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id))
                .ifPresent(output::accept);
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
