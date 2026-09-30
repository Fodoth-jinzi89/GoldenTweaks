package net.fodoth.skina.goldentweaks.mixin.fix.thaumicbases;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tb.common.item.ThaumicSeedItem;

/**
 * {@link ThaumicSeedItem#useOn} hard-codes vanilla farmland as the only valid soil for {@code FARMLAND} seeds
 * (the first {@code BlockState#is} call in the method), so seeds could not be planted on Farmers Delight's
 * rich soil farmland even though the crop itself survives there.
 */
@Mixin(value = ThaumicSeedItem.class, remap = false)
public abstract class ThaumicSeedItemMixin {

    @Redirect(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
                    ordinal = 0
            )
    )
    private static boolean goldentweaks$allowRichSoilFarmland(BlockState state, Block block) {
        return state.is(block)
                || (block == Blocks.FARMLAND && isRichSoilFarmland(state));
    }

    private static boolean isRichSoilFarmland(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals("farmersdelight:rich_soil_farmland");
    }
}
