package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.runtime.EmiReloadManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 去掉屏幕上常驻的那行红字「延迟较大，网络还正常吗？」。
 *
 * <p>EMI 的 {@code EmiReloadManager#step(Component, long worry)} 会设
 * {@code reloadWorry = 当前时间 + worry}，而插件各阶段传进来的 worry 只有 10~500ms
 * （它是按"这一步不该超过这么久"设计的，插件本来是并行投递的）；
 * {@code EmiScreenManager} 渲染时只要 {@code now > reloadWorry} 就画一行红字。
 * 555 模组下合法的阶段动辄几秒，这行字必然常驻。</p>
 *
 * <p>把 worry 抬到一个实际永不触发的大值（不取 {@code Long.MAX_VALUE}，避免相加溢出成负数）。
 * 只影响这一条界面提示，EMI 的阶段日志与超时语义本身不变。</p>
 */
@Mixin(value = EmiReloadManager.class, remap = false)
public abstract class EmiReloadWorryMixin {

    @ModifyVariable(method = "step(Lnet/minecraft/network/chat/Component;J)V", at = @At("HEAD"), argsOnly = true)
    private static long goldentweaks$noReloadWorry(long worry) {
        return Long.MAX_VALUE / 2L;
    }
}
