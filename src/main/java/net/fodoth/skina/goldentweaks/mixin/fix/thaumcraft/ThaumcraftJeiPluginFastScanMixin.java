package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import mezz.jei.api.helpers.IStackHelper;
import net.fodoth.skina.goldentweaks.compat.jei.GTJeiAspectScan;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.integration.jei.ThaumcraftJeiPlugin;

/**
 * {@code collectAspectSources} 对全部三万多物品逐个调用 {@code hasSafeJeiUid}，
 * 而后者对每个物品做两次 {@code getUidForStack} 只为 try/catch 探错、结果丢掉 ——
 * 实测 {@code aspect-build=22199ms}，是 TMRV 那 24.7s 墙钟的一半。
 *
 * <p>无自定义 components 的物品（这里全部都是 {@code item.getDefaultInstance()}）直接放行，
 * 见 {@link GTJeiAspectScan}。带 components 的物品走原逻辑，探错照旧。</p>
 */
@Mixin(value = ThaumcraftJeiPlugin.class, remap = false)
public abstract class ThaumcraftJeiPluginFastScanMixin {

    @Inject(method = "hasSafeJeiUid", at = @At("HEAD"), cancellable = true)
    private static void gt$skipUidProbe(IStackHelper helper, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (GTJeiAspectScan.fastScan(stack)) {
            cir.setReturnValue(true);
        }
    }
}
