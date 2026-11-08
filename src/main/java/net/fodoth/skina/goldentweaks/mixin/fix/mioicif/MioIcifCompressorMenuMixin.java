package net.fodoth.skina.goldentweaks.mixin.fix.mioicif;

import com.miophas.singularity_iteration.common.menu.producer.CompressorElcMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 压缩机菜单的升级槽起始下标修正。
 *
 * <p>压缩机比兄弟机器多一个扩展槽（容器下标 3），4 个升级槽包装的容器下标是 4~7，菜单里对应的
 * <b>菜单下标是 3~6</b>。但 {@code getUpgradeSlotStart()} 返回的是容器下标 4，而基类把它当菜单下标用，
 * 于是升级插件 Shift 转移会从第 2 个升级槽开始、跳过第 1 个。改回菜单下标 3 即可
 * （升级槽包装的容器下标不变，仍是 4~7；{@code isUpgradeSlot} 的「可兼容的升级插件」提示也一并修正）。</p>
 *
 * <p>机器格 / 背包格的边界错位在 {@link MioIcifBaseMenuSlotFixMixin} 里处理。</p>
 */
@Mixin(value = CompressorElcMenu.class, remap = false)
public abstract class MioIcifCompressorMenuMixin {

    @Inject(method = "getUpgradeSlotStart", at = @At("HEAD"), cancellable = true)
    private void gt$fixUpgradeSlotStart(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(3);
    }
}
