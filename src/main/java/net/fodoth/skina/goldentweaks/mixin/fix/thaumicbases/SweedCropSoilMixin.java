package net.fodoth.skina.goldentweaks.mixin.fix.thaumicbases;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tb.common.block.SweedCropBlock;

/**
 * Sweed (芦苇) only accepted grass / dirt / coarse dirt / podzol as soil. Also accept farmland - including
 * Farmers Delight's rich soil farmland - so it can be planted on farmland like any other crop.
 * <p>
 * {@code isSweedSoil} is shared by {@code canSurvive}, {@code randomTick} and {@link tb.common.item.ThaumicSeedItem},
 * so this single hook covers survival, spreading and planting.
 */
@Mixin(value = SweedCropBlock.class, remap = false)
public abstract class SweedCropSoilMixin {

    @Inject(method = "isSweedSoil", at = @At("HEAD"), cancellable = true)
    private static void goldentweaks$allowFarmland(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(Blocks.FARMLAND) || isRichSoilFarmland(state)) {
            cir.setReturnValue(true);
        }
    }

    private static boolean isRichSoilFarmland(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals("farmersdelight:rich_soil_farmland");
    }
}
