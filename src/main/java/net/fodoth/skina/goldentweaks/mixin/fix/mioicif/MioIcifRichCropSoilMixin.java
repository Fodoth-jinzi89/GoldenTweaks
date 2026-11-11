package net.fodoth.skina.goldentweaks.mixin.fix.mioicif;

import com.miophas.singularity_iteration.common.block.crop.enriched.UniformCropTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 富集种子作物（{@link UniformCropTemplate} 及其子类）只接受原版耕地（{@code instanceof FarmBlock}），
 * 导致它们无法种在 Farmers Delight 的沃土（{@code farmersdelight:rich_soil}，本身是普通方块）上，
 * 甚至 {@code canSurvive} 也会判定失败而掉落。
 *
 * <p>沃土农场（{@code farmersdelight:rich_soil_farmland}）虽然继承自 {@code FarmBlock}、作物本可存活，
 * 但种子放置仍被 {@code RichSeedItem} 写死的原版耕地判断拦住（见 {@link MioIcifRichSeedItemMixin}）。
 * 这里把两种沃土都补充为有效土壤，与 Thaumic Bases 作物的处理保持一致。</p>
 */
@Mixin(value = UniformCropTemplate.class, remap = false)
public abstract class MioIcifRichCropSoilMixin {

    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void gt$allowRichSoil(BlockState state, BlockGetter level, BlockPos pos,
                                  CallbackInfoReturnable<Boolean> cir) {
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        if (id.equals("farmersdelight:rich_soil") || id.equals("farmersdelight:rich_soil_farmland")) {
            cir.setReturnValue(true);
        }
    }
}
