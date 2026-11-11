package net.fodoth.skina.goldentweaks.mixin.fix.mioicif;

import com.miophas.singularity_iteration.common.item.crop.enriched.RichSeedItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@link RichSeedItem#tryPlantOnFarmland} 把有效土壤写死为原版耕地（{@code state.is(Blocks.FARMLAND)}），
 * 于是富集种子无法种在 Farmers Delight 的沃土 / 沃土农场上。这里放宽该判断。
 *
 * <p>作物能否存活由 {@link MioIcifRichCropSoilMixin} 在 {@code mayPlaceOn} 上处理。</p>
 */
@Mixin(value = RichSeedItem.class, remap = false)
public abstract class MioIcifRichSeedItemMixin {

    @Redirect(
            method = "tryPlantOnFarmland",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
                    ordinal = 0
            )
    )
    private boolean gt$allowRichSoil(BlockState state, Block block) {
        if (state.is(block)) {
            return true;
        }
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        return id.equals("farmersdelight:rich_soil") || id.equals("farmersdelight:rich_soil_farmland");
    }
}
