package net.fodoth.skina.goldentweaks.mixin.fix.touhoulittlemaidspell.block.entity;

import com.github.yimeng261.maidspell.block.entity.YueLinglanBlockEntity;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = YueLinglanBlockEntity.class, remap = false)
public abstract class YueLinglanBlockEntityMixin {

    @Inject(method = "tickStructureTrail", at = @At("HEAD"), cancellable = true, remap = false)
    private void gt$disableStructureTrail(ServerLevel level, BlockPos pos, long gameTime, CallbackInfo ci) {
        if (GoldenTweaksCommonConfig.DISABLE_YUE_LINGLAN_STRUCTURE_TRAIL.get()) {
            ci.cancel();
        }
    }
}
