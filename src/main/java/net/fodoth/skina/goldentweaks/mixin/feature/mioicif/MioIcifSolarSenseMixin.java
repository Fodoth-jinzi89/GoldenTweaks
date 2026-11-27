package net.fodoth.skina.goldentweaks.mixin.feature.mioicif;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.miophas.singularity_iteration.common.blockentity.generator.mio_icif_solar_generator;
import com.miophas.singularity_iteration.common.energy.SolarProfile;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTMioIcifCompactSpace;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Inside a Compact Machines dimension every MIO-ICIF solar tier (basic, advanced, hybrid, quantum, METS and the
 * photon-resonance generators) ignores time of day, sky obstruction and weather and produces its daytime peak.
 *
 * <p>mio_icif 0.1.11 replaced the old per-class {@code calculateSkyLight} / {@code canGenerate} /
 * {@code scexCurrentRate} hooks with one pipeline: {@code sense(Level)} reads the environment and hands it to the
 * static {@code evaluate(...)}, whose result is then scaled by the untouched {@code outputScale()} path. Rewriting
 * the environment arguments of that single call reproduces exactly what the mod computes at solar noon under a
 * clear, unobstructed sky: {@code dayTime == 6000}, {@code exposure == OPEN (0)}, {@code skyLight == 15}, no
 * precipitation, no rain and no thunder.</p>
 *
 * <p>{@code sense} is the only caller of {@code evaluate}, so this one hook covers every solar tier — including the
 * advanced/METS subclasses that previously needed their own {@code canGenerate} overrides.</p>
 */
@Mixin(value = mio_icif_solar_generator.class, remap = false)
public abstract class MioIcifSolarSenseMixin {

    /** Solar noon: {@code SolarPhysics.elevation(6000)} is the 90° peak. */
    @Unique
    private static final long gt$SOLAR_NOON = 6000L;

    /** {@code mio_icif_solar_generator.OPEN}: the panel sees the whole sky. */
    @Unique
    private static final int gt$OPEN = 0;

    @Unique
    private static final int gt$FULL_SKY_LIGHT = 15;

    @WrapOperation(
            method = "sense",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/miophas/singularity_iteration/common/blockentity/generator/mio_icif_solar_generator;evaluate(Lcom/miophas/singularity_iteration/common/energy/SolarProfile;IIJIIZZFFF)Lcom/miophas/singularity_iteration/common/blockentity/generator/mio_icif_solar_generator$Reading;"
            )
    )
    private mio_icif_solar_generator.Reading gt$compactSpacePeakReading(
            SolarProfile profile, int dayPower, int corePower, long dayTime, int exposure, int skyLight,
            boolean hasPrecipitation, boolean raining, float rainLevel, float thunderLevel, float baseTemperature,
            Operation<mio_icif_solar_generator.Reading> original) {

        if (!GTMioIcifCompactSpace.isCompact(((BlockEntity) (Object) this).getLevel())) {
            return original.call(profile, dayPower, corePower, dayTime, exposure, skyLight,
                    hasPrecipitation, raining, rainLevel, thunderLevel, baseTemperature);
        }
        return original.call(profile, dayPower, corePower, gt$SOLAR_NOON, gt$OPEN, gt$FULL_SKY_LIGHT,
                false, false, 0.0F, 0.0F, baseTemperature);
    }
}
