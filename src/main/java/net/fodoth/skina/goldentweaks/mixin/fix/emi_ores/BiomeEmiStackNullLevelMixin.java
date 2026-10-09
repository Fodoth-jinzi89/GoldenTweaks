package net.fodoth.skina.goldentweaks.mixin.fix.emi_ores;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * emi_ores 的 {@code BiomeEmiStack#getId} 经 {@code Minecraft.getInstance().level.registryAccess()}
 * 取生物群系注册表。退出世界后 level 为空，而 EMI 的搜索索引恰好在此刻烘焙，
 * 每个群系堆栈都会 NPE，日志刷出成片的
 * {@code EMI caught an exception while baking search for ... BiomeEmiStack.getId/getName}。
 *
 * <p>没有世界时返回一个稳定的占位 id：构造器与 {@code getName}/{@code getTooltip} 只依赖该 id 非空，
 * 因此不再连锁崩溃；有世界时行为不变。</p>
 */
@Mixin(targets = "cc.abbie.emi_ores.compat.emi.stack.BiomeEmiStack", remap = false)
public class BiomeEmiStackNullLevelMixin {

    @Inject(method = "getId", at = @At("HEAD"), cancellable = true, require = 0)
    private void gt$fallbackIdWithoutLevel(CallbackInfoReturnable<ResourceLocation> cir) {

        if (Minecraft.getInstance().level == null) {
            cir.setReturnValue(ResourceLocation.fromNamespaceAndPath("emi_ores", "missing"));
        }
    }
}
