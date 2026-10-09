package net.fodoth.skina.goldentweaks.mixin.fix.ae2cs;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ae2cs 的「输入槽」包装器把「模拟抽取」和「实际抽取」实现成了两套结果：
 * 物品输入槽 {@code AppEngInvComponent$2#extractItem} 在 {@code simulate=true} 时返回一份物品副本、
 * {@code simulate=false} 时返回空；通用存储（流体等）输入槽 {@code GenericStackInvComponent$1#extract}
 * 同样在 SIMULATE 返回数量、MODULATE 返回 0。这违反了 ItemHandler / GenericInternalInventory 的约定
 * ——模拟抽取必须与实际抽取一致。
 *
 * <p>Create 的 {@code ItemHelper#extract} 先用 {@code simulate=true} 试探并把试探结果当作抽到的物品，
 * 随后才另做一次实际抽取；对输入槽来说实际抽取是空操作，于是下方溜槽每个 tick 都能“抽走”一份副本，
 * 表现为无限刷物品。ae2cs 官方指南也明确「输入槽永远不会被物流或管道抽取」，
 * 因此这里直接把输入槽包装器统一改为「永远抽不到」，使模拟与实际一致。</p>
 */
@Mixin(targets = {
        "io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent$2",
        "io.github.lounode.ae2cs.common.machine.component.GenericStackInvComponent$1"
}, remap = false)
public abstract class AECsInputPortExtractMixin {

    @Inject(method = "extractItem", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void goldentweaks$blockItemInputExtraction(int slot, int amount, boolean simulate,
                                                       CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(ItemStack.EMPTY);
    }

    @Inject(method = "extract", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void goldentweaks$blockGenericInputExtraction(int slot, AEKey key, long amount, Actionable mode,
                                                          CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue(0L);
    }
}
