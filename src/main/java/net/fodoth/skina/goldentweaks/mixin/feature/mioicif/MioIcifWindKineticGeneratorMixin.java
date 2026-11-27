package net.fodoth.skina.goldentweaks.mixin.feature.mioicif;

import com.miophas.singularity_iteration.common.blockentity.kuentity.kugenerator.mio_icif_Wind_Kinetic_Generator;
import com.miophas.singularity_iteration.core.api.item.IKineticRotor;
import com.miophas.singularity_iteration.core.runtime.world.WindSim;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTMioIcifCompactSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Inside a Compact Machines dimension the wind kinetic generator ignores rotor obstruction and uses the wind
 * of the highest-wind outdoor position in clear weather (the peak of the wind curve, multiplier 1).
 *
 * <p>{@code calculateKineticOutput} multiplies {@code windStrength} by {@code obstructionFactor},
 * {@code facingFactor} and {@code wakeFactor}, so the compact path has to set all four — leaving the factors at
 * their default {@code 0} would zero the output.</p>
 */
@Mixin(value = mio_icif_Wind_Kinetic_Generator.class, remap = false)
public abstract class MioIcifWindKineticGeneratorMixin {

    @Shadow
    private int windStrength;

    @Shadow
    private int lastObstruction;

    @Shadow
    private double obstructionFactor;

    @Shadow
    private double facingFactor;

    @Shadow
    private double wakeFactor;

    @Inject(method = "updateWindStrength", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceWindStrength(CallbackInfo ci) {
        if (((BlockEntity) (Object) this).getLevel() instanceof ServerLevel server
                && GTMioIcifCompactSpace.isCompact(server)) {
            this.lastObstruction = 0;
            this.obstructionFactor = 1.0D;
            this.facingFactor = 1.0D;
            this.wakeFactor = 1.0D;
            boolean hasRotor = ((mio_icif_Wind_Kinetic_Generator) (Object) this)
                    .getRotorStack().getItem() instanceof IKineticRotor;
            this.windStrength = hasRotor
                    ? (int) Math.round(WindSim.get(server).getWindStrength() * WindSim.globalMultiplier())
                    : 0;
            ci.cancel();
        }
    }

    @Inject(method = "calculateKineticOutput", at = @At("RETURN"), cancellable = true)
    private void goldentweaks$compactSpaceOutput(CallbackInfoReturnable<Integer> cir) {
        int output = cir.getReturnValue();
        if (output > 0 && ((BlockEntity) (Object) this).getLevel() instanceof ServerLevel server
                && GTMioIcifCompactSpace.isCompact(server)) {
            cir.setReturnValue((int) Math.round(output * GTMioIcifCompactSpace.windFraction()));
        }
    }
}
