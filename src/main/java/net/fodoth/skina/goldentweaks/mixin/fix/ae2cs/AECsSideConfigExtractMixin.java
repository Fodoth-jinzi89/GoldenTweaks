package net.fodoth.skina.goldentweaks.mixin.fix.ae2cs;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import io.github.lounode.ae2cs.common.machine.component.SidePolicy;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ae2cs 侧面配置包装器 {@code SideConfigComponent$3#extractItem} / {@code SideConfigComponent$1#extract}
 * 在 {@code simulate / Actionable.SIMULATE} 分支里无视侧面策略，只要实际能抽就返回可抽取数量；
 * 而真正抽取分支才会检查 {@code SidePolicy.allowExtract()}。于是当某一面被设成「仅输入」（不允许抽取）时，
 * 模拟抽取照样汇报可抽取，实际抽取却返回空——与输入槽包装器同类的约定不一致问题。
 * Create 溜槽等按「先模拟后实际」工作的外部物流会因此无限复制输出物。
 *
 * <p>这里让模拟分支与真正的抽取分支遵守同一套侧面策略：不允许抽取的面对模拟抽取同样返回空。</p>
 */
@Mixin(targets = {
        "io.github.lounode.ae2cs.common.machine.component.SideConfigComponent$1",
        "io.github.lounode.ae2cs.common.machine.component.SideConfigComponent$3"
}, remap = false)
public abstract class AECsSideConfigExtractMixin {

    @Shadow
    @Final
    private SidePolicy val$policy;

    @Inject(method = "extractItem", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void goldentweaks$honorItemSidePolicyOnSimulate(int slot, int amount, boolean simulate,
                                                            CallbackInfoReturnable<ItemStack> cir) {
        if (simulate && !this.val$policy.allowExtract()) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "extract", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void goldentweaks$honorGenericSidePolicyOnSimulate(int slot, AEKey key, long amount, Actionable mode,
                                                               CallbackInfoReturnable<Long> cir) {
        if (mode == Actionable.SIMULATE && !this.val$policy.allowExtract()) {
            cir.setReturnValue(0L);
        }
    }
}
