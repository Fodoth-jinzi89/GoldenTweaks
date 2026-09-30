package net.fodoth.skina.goldentweaks.mixin.shut;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hides c2me's OpenCL acceleration "Compiling …" overlay.
 *
 * <p>c2me's OpenCL module publishes progress into a global holder
 * ({@code GlobalProgressStash.PROGRESS_TEXT}) while it compiles OpenCL programs, and its
 * {@code MixinGameRenderer} draws that string as wrapped text near the bottom of the screen for as long as it is
 * non-null. The writes all live in {@code OpenCLDevice} ({@code compileProgramAsync} /
 * {@code compileProgram0}), which is a plain class rather than a mixin, so we can swallow them cleanly here.</p>
 *
 * <p>Keeping the field null means the render path early-returns and nothing is drawn; the OpenCL compilation
 * itself keeps running normally. All four writes actually sit in the synthetic
 * {@code lambda$compileProgramAsync$0} (the outer {@code compileProgramAsync}/{@code compileProgram0} are listed
 * too, in case a future build inlines them). {@code targets} is addressed by name, so the c2me jar is not needed
 * at compile time, and {@code require = 0} means a future rename degrades to "message visible again" instead of a
 * crash.</p>
 */
@Mixin(targets = "com.ishland.c2me.opts.accel.opencl.common.gen.OpenCLDevice", remap = false)
public class C2meOpenClProgressOverlayMixin {

    @Redirect(
            method = {"lambda$compileProgramAsync$0", "compileProgramAsync", "compileProgram0"},
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.PUTSTATIC,
                    target = "Lcom/ishland/c2me/opts/accel/opencl/common/progress/GlobalProgressStash;PROGRESS_TEXT:Ljava/lang/String;"
            ),
            require = 0
    )
    private static void gt$hideProgressText(String value) {
        // Swallow the assignment: GlobalProgressStash.PROGRESS_TEXT stays null, so c2me's
        // MixinGameRenderer never draws the "Compiling …" text.
    }
}
