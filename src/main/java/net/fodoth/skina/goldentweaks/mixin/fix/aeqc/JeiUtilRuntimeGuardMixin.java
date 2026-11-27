package net.fodoth.skina.goldentweaks.mixin.fix.aeqc;

import appeng.api.stacks.GenericStack;
import com.github.debris.aeqc.util.JeiUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Refuses to ask JEI for the hovered ingredient before JEI has a runtime.
 *
 * <p>{@code JeiUtil.getHoveredStack()} dereferences its {@code jeiRuntime} field on its very first
 * instruction, with no null check. That field is only assigned when JEI hands over its runtime,
 * which happens after the world has loaded and EMI/JEI have finished one rebuild — so any hotkey
 * pressed while the game is still entering a save throws instead of doing nothing.</p>
 *
 * <p>Entering a save and immediately firing a craft key is exactly that window, and the throw
 * surfaces as {@code Unreported exception thrown!} on the render thread. All three of AEQC's
 * callers ({@code FastPull}, {@code FastAutoCraft}, {@code SearchTweaks}) funnel through here, so
 * one guard covers them all: with no runtime there is nothing under the mouse to act on anyway.</p>
 */
@Mixin(value = JeiUtil.class, remap = false)
public abstract class JeiUtilRuntimeGuardMixin {

    @Inject(method = "getHoveredStack", at = @At("HEAD"), cancellable = true)
    private static void goldentweaks$requireRuntime(CallbackInfoReturnable<GenericStack> cir) {
        if (JeiUtil.jeiRuntime == null) {
            // Null is what a miss returns anyway, and every caller already treats it as "nothing
            // hovered", so the key press is ignored rather than aborting the tick.
            cir.setReturnValue(null);
        }
    }
}
