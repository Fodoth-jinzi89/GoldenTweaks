package net.fodoth.skina.goldentweaks.mixin.fix.thaumictinkerer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumic.tinkerer.common.item.InfusedSeedsItem;
import thaumic.tinkerer.common.registry.TTBlocks;

/**
 * Infused seeds could only be planted on Thaumic Tinkerer's infused farmland, and planting always rewrote the
 * clicked block into infused farmland. Let rich soil farmland work as soil too, without replacing it.
 * <p>
 * The first {@code BlockState#getBlock} call in {@code useOn} is the "is this valid soil?" gate, and the first
 * {@code Level#setBlock} call is the farmland rewrite that follows it.
 */
@Mixin(value = InfusedSeedsItem.class, remap = false)
public abstract class InfusedSeedsItemMixin {

    @Redirect(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;",
                    ordinal = 0
            )
    )
    private static Block goldentweaks$treatRichSoilAsInfusedFarmland(BlockState state) {
        return goldentweaks$isRichSoilFarmland(state) ? TTBlocks.INFUSED_FARMLAND.get() : state.getBlock();
    }

    @Redirect(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
                    ordinal = 0
            )
    )
    private static boolean goldentweaks$keepRichSoilFarmland(Level level, BlockPos pos, BlockState state, int flags) {
        if (goldentweaks$isRichSoilFarmland(level.getBlockState(pos))) {
            // Rich soil farmland is strictly better than infused farmland - do not overwrite it.
            return true;
        }
        return level.setBlock(pos, state, flags);
    }

    private static boolean goldentweaks$isRichSoilFarmland(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals("farmersdelight:rich_soil_farmland");
    }
}
