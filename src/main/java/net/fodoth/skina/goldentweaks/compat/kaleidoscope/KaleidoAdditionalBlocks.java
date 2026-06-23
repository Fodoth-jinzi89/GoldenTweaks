package net.fodoth.skina.goldentweaks.compat.kaleidoscope;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class KaleidoAdditionalBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(GoldenTweaks.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredBlock<GarbageStationFloorBlock> GARBAGE_STATION_FLOOR =
            BLOCKS.register(
                    "garbage_station_floor",
                    () -> new GarbageStationFloorBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(1.5F, 6.0F)
                                    .sound(SoundType.STONE)
                                    .requiresCorrectToolForDrops()
                                    .destroyTime(1.5F)
                    )
            );

    public static final DeferredItem<BlockItem> GARBAGE_STATION_FLOOR_ITEM =
            ITEMS.register(
                    "garbage_station_floor",
                    () -> new BlockItem(
                            GARBAGE_STATION_FLOOR.get(),
                            new Item.Properties()
                    )
            );

    private KaleidoAdditionalBlocks() {
    }
}