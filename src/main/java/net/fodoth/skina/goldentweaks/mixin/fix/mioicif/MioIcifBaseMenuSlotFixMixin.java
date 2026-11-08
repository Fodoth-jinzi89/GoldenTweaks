package net.fodoth.skina.goldentweaks.mixin.fix.mioicif;

import com.miophas.singularity_iteration.common.menu.base.mio_icif_base_menu;
import com.miophas.singularity_iteration.common.menu.producer.CompressorElcMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 压缩机菜单的「机器格 / 背包格」边界修正。
 *
 * <p>压缩机的方块实体比兄弟机器多一个扩展槽（{@code EXTRA_SLOT}，容器下标 3），菜单没有把它加进界面：
 * 槽位列表只有 7 个机器格（输入 0 / 电池 1 / 输出 2 / 升级 4~7），但基类 {@code mio_icif_base_menu} 用的
 * {@code machineSlotCount} 既是「客户端合成处理器」的格数（必须是 8，否则渲染第 4 个升级槽即容器下标 7 时
 * 抛 {@code Slot 7 not in valid range - [0,7)}），又被当成「机器格的菜单下标上界」。</p>
 *
 * <p>于是菜单下标 7（玩家背包第一格）被 {@code isMachineSlot} 判成机器槽：悬停显示「可兼容的升级插件」，
 * Shift 转移走「机器→背包」分支被丢进快捷栏。这些判定都定义在基类里，所以在基类上按菜单实例改：
 * 压缩机真正的机器格只有菜单下标 0~6。</p>
 *
 * <p>这些方法只按静态类型编译在本基类中（子类没有覆写），因此注入基类即可对压缩机实例生效。</p>
 */
@Mixin(value = mio_icif_base_menu.class, remap = false)
public abstract class MioIcifBaseMenuSlotFixMixin {

    @Inject(method = "isMachineSlot", at = @At("HEAD"), cancellable = true)
    private void gt$compressorMachineSlotBoundary(int index, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof CompressorElcMenu) {
            cir.setReturnValue(index >= 0 && index < 7);
        }
    }

    @Inject(method = "getPlayerInventoryStart", at = @At("HEAD"), cancellable = true)
    private void gt$compressorPlayerInventoryStart(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof CompressorElcMenu) {
            cir.setReturnValue(7);
        }
    }

    @Inject(method = "getMachineSlotRanges", at = @At("HEAD"), cancellable = true)
    private void gt$compressorMachineSlotRanges(CallbackInfoReturnable<java.util.List<int[]>> cir) {
        if ((Object) this instanceof CompressorElcMenu) {
            cir.setReturnValue(java.util.List.of(new int[] { 0, 7 }));
        }
    }
}
