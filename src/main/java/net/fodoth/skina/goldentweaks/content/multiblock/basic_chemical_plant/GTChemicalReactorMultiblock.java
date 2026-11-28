package net.fodoth.skina.goldentweaks.content.multiblock.basic_chemical_plant;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineBlock;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineBlockEntity;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineMenu;

/**
 * Registers the basic chemical reaction kettle as an {@code avaritia_more_items} multiblock host.
 *
 * <p>This lives in the compat package because {@link GTMachineBlock} extends AMI's
 * {@code AbstractMultiblockBlock}. Touching that class without AMI installed throws
 * {@code NoClassDefFoundError}, so {@link GoldenTweaks} only calls {@link #register} once the mod
 * is known to be present; the plain {@code GTChemicalReactorBlock} takes over otherwise.</p>
 */
public final class GTChemicalReactorMultiblock {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(GoldenTweaks.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GoldenTweaks.MODID);

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, GoldenTweaks.MODID);

    /**
     * The kettle's own screen. The block position travels in the opening packet, which is what
     * {@code useWithoutItem} writes and the factory here reads back to resolve the tile.
     */
    public static final DeferredHolder<MenuType<?>, MenuType<GTMachineMenu>> MENU =
            MENU_TYPES.register("chemical_reactor", () -> IMenuTypeExtension.create(
                    (containerId, inventory, data) ->
                            new GTMachineMenu(containerId, inventory, data.readBlockPos())));

    public static final DeferredBlock<GTMachineBlock> MACHINE =
            BLOCKS.register("chemical_plant_controller",
                    () -> new GTMachineBlock(GTMultiblockBlocks.properties(),
                            "chemical_reactor", GTMachineBlockEntity::new,
                            GTMultiblockBlocks.SHELL_PE));

    public static final DeferredItem<BlockItem> MACHINE_ITEM =
            ITEMS.register("chemical_plant_controller",
                    () -> new GTMachineItem(MACHINE.get(), new Item.Properties()));

    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GTMachineBlockEntity>>
            MACHINE_ENTITY =
            BLOCK_ENTITIES.register(
                    "chemical_plant_controller",
                    () -> BlockEntityType.Builder.of(
                            GTMachineBlockEntity::new,
                            MACHINE.get()
                    ).build(null)
            );

    private GTChemicalReactorMultiblock() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENU_TYPES.register(eventBus);
    }
}
