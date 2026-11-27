package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.block.generator.mio_icif_Block_Nuclear_Reactor_Generator;
import com.miophas.singularity_iteration.common.block.mio_icif_entity_block;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 「能量等级」提示来自基类 {@code mio_icif_entity_block#addVoltageTierTooltip}，
 * 内容按方块实体的电缆等级算出（核反应堆是 EV）。核电产能调整开启后，
 * 等级已不再代表实际产出，这里对核反应堆发电机方块直接跳过该提示。
 */
@Mixin(value = mio_icif_entity_block.class, remap = false)
public abstract class MioIcifNuclearReactorTooltipMixin {

    @Inject(method = "addVoltageTierTooltip", at = @At("HEAD"), cancellable = true)
    private void gt$hideReactorVoltageTier(ItemStack stack, List<Component> tooltip, CallbackInfo ci) {
        if (GoldenTweaksCommonConfig.isMioIcifReactorOutputAdjusted()
                && (Object) this instanceof mio_icif_Block_Nuclear_Reactor_Generator) {
            ci.cancel();
        }
    }
}
