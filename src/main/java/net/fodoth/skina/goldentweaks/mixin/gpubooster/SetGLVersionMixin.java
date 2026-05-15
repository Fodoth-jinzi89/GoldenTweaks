package net.fodoth.skina.goldentweaks.mixin.gpubooster;


import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


@Mixin(Window.class)
public abstract class SetGLVersionMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V",
                    ordinal = 4,
                    remap = false
            ),
            index = 1
    )
    private int goldenTweaks$major(int value) {
        return 4;
    }

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V",
                    ordinal = 5,
                    remap = false
            ),
            index = 1
    )
    private int goldenTweaks$minor(int value) {
        return goldenTweaks$isMacOS() ? 1 : 6;
    }
    @Unique
    private static boolean goldenTweaks$isMacOS() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("mac");
    }
}