package net.fodoth.skina.goldentweaks.compat.compactmachines;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CompactMachinesAdditionalBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(GoldenTweaks.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(-1.0F, 3600000.0F)
                .lightLevel(state -> 15)
                .noLootTable()
                .pushReaction(PushReaction.BLOCK);
    }

    public static final DeferredBlock<Block> SKY_BLOCK = BLOCKS.register("sky_block", () -> new Block(properties()));
    public static final DeferredBlock<Block> STARRY_BLOCK = BLOCKS.register("starry_block", () -> new Block(properties()));

    public static final DeferredItem<BlockItem> SKY_BLOCK_ITEM = ITEMS.register("sky_block", () -> new BlockItem(SKY_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> STARRY_BLOCK_ITEM = ITEMS.register("starry_block", () -> new BlockItem(STARRY_BLOCK.get(), new Item.Properties()));

    private CompactMachinesAdditionalBlocks() {
    }
}
