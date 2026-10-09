package net.fodoth.skina.goldentweaks.mixin.fix.ftbquests;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * AcceleratedRendering 的 FTB Library 批处理特性（acceleratedrendering.feature.ftb）会在
 * FTB 界面绘制期间把所有填充/贴图/物品/文字收集起来，最后统一用计算着色器回放。这套回放
 * 打乱了与原版绘制次序、裁剪区域和物品打光的先后关系，使任务书出现：
 *
 * <ul>
 *     <li>章节图标里的方块/模型失去侧向打光（只剩正面平光）；</li>
 *     <li>物品模型（如「欢迎游玩」的手杖）溢出任务外框；</li>
 *     <li>编辑界面矩形缺失、文字出现虚边。</li>
 * </ul>
 *
 * <p>该特性是否启用只由 {@code ModsFeature#shouldAccelerateFtb} 决定，而这个方法只被
 * AcceleratedRendering 自己的 {@code ftb.mixins.BaseScreenMixin} 调用（即仅作用于 FTB
 * Library 的 {@code BaseScreen}，任务书的 {@code QuestScreen} 与配置编辑的
 * {@code EditConfigScreen} 都在其中）。这里让它恒为 {@code false}，等于对 FTB 界面回退到
 * 原版渲染管线；热栏、容器等其它 GUI 批处理不受影响。</p>
 */
@Mixin(targets = "com.github.argon4w.acceleratedrendering.features.mods.ModsFeature", remap = false)
public class FtbAccelerationDisableMixin {

    @Inject(method = "shouldAccelerateFtb", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gt$disableFtbBatching(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
