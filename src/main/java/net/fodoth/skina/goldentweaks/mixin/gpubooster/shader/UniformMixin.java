package net.fodoth.skina.goldentweaks.mixin.gpubooster.shader;

import com.mojang.blaze3d.shaders.Uniform;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.gpubooster.api.GTGL;
import net.fodoth.skina.goldentweaks.util.DSAMode;
import org.lwjgl.opengl.GL46;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@Mixin(Uniform.class)
public abstract class UniformMixin {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    private int location;

    @Shadow
    @Final
    private int count;

    @Shadow
    @Final
    private int type;

    @Shadow
    @Final
    private IntBuffer intValues;

    @Shadow
    @Final
    private FloatBuffer floatValues;

    @Inject(
            method = "uploadAsInteger",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$uploadIntsDSA(CallbackInfo ci) {

        if (!GoldenTweaksClientConfig.hasDSA(DSAMode.ALL)) {
            return;
        }

        int program = GTGL.CURRENT_PROGRAM.get();

        if (program == 0) {
            return;
        }

        this.intValues.rewind();

        switch (this.type) {

            case 0 -> GL46.glProgramUniform1iv(
                    program,
                    this.location,
                    this.intValues
            );

            case 1 -> GL46.glProgramUniform2iv(
                    program,
                    this.location,
                    this.intValues
            );

            case 2 -> GL46.glProgramUniform3iv(
                    program,
                    this.location,
                    this.intValues
            );

            case 3 -> GL46.glProgramUniform4iv(
                    program,
                    this.location,
                    this.intValues
            );

            default -> LOGGER.warn(
                    "Uniform.upload called, but count value ({}) is not in the range of 1 to 4. Ignoring.1",
                    this.count
            );
        }

        ci.cancel();
    }

    @Inject(
            method = "uploadAsFloat",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$uploadFloatsDSA(CallbackInfo ci) {

        if (!GoldenTweaksClientConfig.hasDSA(DSAMode.ALL)) {
            return;
        }

        int program = GTGL.CURRENT_PROGRAM.get();

        if (program == 0) {
            return;
        }

        this.floatValues.rewind();

        switch (this.type) {

            case 4 -> GL46.glProgramUniform1fv(
                    program,
                    this.location,
                    this.floatValues
            );

            case 5 -> GL46.glProgramUniform2fv(
                    program,
                    this.location,
                    this.floatValues
            );

            case 6 -> GL46.glProgramUniform3fv(
                    program,
                    this.location,
                    this.floatValues
            );

            case 7 -> GL46.glProgramUniform4fv(
                    program,
                    this.location,
                    this.floatValues
            );

            default -> LOGGER.warn(
                    "Uniform.upload called, but count value ({}) is not in the range of 1 to 4. Ignoring.",
                    this.count
            );
        }

        ci.cancel();
    }

    @Inject(
            method = "uploadAsMatrix",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$uploadMatrixDSA(CallbackInfo ci) {

        if (!GoldenTweaksClientConfig.hasDSA(DSAMode.ALL)) {
            return;
        }

        int program = GTGL.CURRENT_PROGRAM.get();

        if (program == 0) {
            return;
        }

        this.floatValues.clear();

        switch (this.type) {

            case 8 -> GL46.glProgramUniformMatrix2fv(
                    program,
                    this.location,
                    false,
                    this.floatValues
            );

            case 9 -> GL46.glProgramUniformMatrix3fv(
                    program,
                    this.location,
                    false,
                    this.floatValues
            );

            case 10 -> GL46.glProgramUniformMatrix4fv(
                    program,
                    this.location,
                    false,
                    this.floatValues
            );
        }

        ci.cancel();
    }
}