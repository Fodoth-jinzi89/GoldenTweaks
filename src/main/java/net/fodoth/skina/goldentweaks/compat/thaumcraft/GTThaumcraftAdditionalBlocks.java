package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GTThaumcraftAdditionalBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(GoldenTweaks.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    public static final DeferredBlock<GTInfusionIntercepterBlock> INFUSION_INTERCEPTER =
            BLOCKS.register(
                    "infusion_intercepter",
                    () -> new GTInfusionIntercepterBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(1.0F, 1.5F)
                                    .sound(SoundType.STONE)
                                    .requiresCorrectToolForDrops()
                    )
            );

    public static final DeferredItem<BlockItem> INFUSION_INTERCEPTER_ITEM =
            ITEMS.register(
                    "infusion_intercepter",
                    () -> new BlockItem(
                            INFUSION_INTERCEPTER.get(),
                            new Item.Properties()
                    )
            );

    private GTThaumcraftAdditionalBlocks() {
    }
}
