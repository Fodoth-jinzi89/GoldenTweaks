package net.fodoth.skina.goldentweaks.mixin.feature.mioicif;

import com.miophas.singularity_iteration.common.blockentity.generator.mio_icif_wind_generator;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTMioIcifCompactSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Inside a Compact Machines dimension the wind generator ignores height, weather and obstacles, and always
 * produces what it would produce outside at the maximum build height with maximum wind on a clear day.
 */
@Mixin(value = mio_icif_wind_generator.class, remap = false)
public abstract class MioIcifWindGeneratorMixin {

    @Shadow
    private int windStrength;

    @Shadow
    private int obstacleCount;

    @Shadow
    private int effectiveHeight;

    @Inject(method = "updateWindStrength", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceWindStrength(CallbackInfo ci) {
        if (GTMioIcifCompactSpace.isCompact(((BlockEntity) (Object) this).getLevel())) {
            this.windStrength = mio_icif_wind_generator.MAX_WIND_STRENGTH;
            ci.cancel();
        }
    }

    @Inject(method = "calculateEnergyOutput", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceEnergyOutput(Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (GTMioIcifCompactSpace.isCompact(level)) {
            //The compact dimension itself is only 48 blocks tall, so simulate the outdoor sky height.
            MinecraftServer server = level.getServer();
            this.obstacleCount = 0;
            this.effectiveHeight = server != null
                    ? server.overworld().getMaxBuildHeight()
                    : level.getMaxBuildHeight();
            double output = (double) mio_icif_wind_generator.MAX_WIND_STRENGTH
                    * Math.max(0, this.effectiveHeight - 64) / 750.0D
                    * GTMioIcifCompactSpace.windFraction();
            cir.setReturnValue(Math.max(0, (int) Math.round(output)));
        }
    }
}
