package net.fodoth.skina.goldentweaks.content.multiblock.vacuum_cooler;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
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
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineMenu;

/**
 * Registers the vacuum cooler: its block, item, block entity and window.
 *
 * <p>Same shape as the advanced kettle's registration. The block class is the basic kettle's,
 * unchanged — it places, rotates, opens a menu and projects its shape the same way — and every
 * difference arrives through the constructor: a distinct structure file and the aluminium casing.</p>
 */
public final class GTVacuumCoolerMultiblock {

    public static final String ID = "vacuum_cooler_controller";

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(GoldenTweaks.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GoldenTweaks.MODID);

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, GoldenTweaks.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GTMachineMenu>> MENU =
            MENU_TYPES.register("vacuum_cooler", () -> IMenuTypeExtension.create(
                    (containerId, inventory, data) ->
                            GTMachineMenu.create(menuType(), containerId, inventory,
                                    data.readBlockPos())));

    /**
     * The menu type, read through a call rather than the field.
     *
     * <p>The factory above is stored inside {@code MENU} itself, so naming the field from its own
     * initializer would be a self-reference the compiler rejects. A method body is deferred until
     * the factory actually runs, by which point the field is set.</p>
     */
    private static MenuType<?> menuType() {
        return MENU.get();
    }

    public static final DeferredBlock<GTMachineBlock> MACHINE =
            BLOCKS.register(ID,
                    () -> new GTMachineBlock(GTMultiblockBlocks.properties(),
                            GTVacuumCoolerBlockEntity.MACHINE_ID,
                            GTVacuumCoolerBlockEntity::new,
                            GTMultiblockBlocks.SHELL_ALUMINUM));

    public static final DeferredItem<BlockItem> MACHINE_ITEM =
            ITEMS.register(ID,
                    () -> new GTVacuumCoolerItem(MACHINE.get(), new Item.Properties(),
                            "block.goldentweaks.shell_aluminum"));

    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<GTVacuumCoolerBlockEntity>> MACHINE_ENTITY =
            BLOCK_ENTITIES.register(ID,
                    () -> BlockEntityType.Builder.of(
                            GTVacuumCoolerBlockEntity::new,
                            MACHINE.get()
                    ).build(null));

    private GTVacuumCoolerMultiblock() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENU_TYPES.register(eventBus);
        GTVacuumCoolingRecipes.register(eventBus);
    }
}
