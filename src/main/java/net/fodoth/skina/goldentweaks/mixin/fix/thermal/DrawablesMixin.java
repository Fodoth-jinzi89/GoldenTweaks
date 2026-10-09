package net.fodoth.skina.goldentweaks.mixin.fix.thermal;

import cofh.thermal.lib.compat.jei.Drawables;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 修复流体进度箭头：Thermal 把「底轨」与「填充」两套贴图在流体配方里对调使用
 * （{@code progressFluidBackground = getProgressFill}、{@code progressFluid = getProgress}），
 * 于是流体配方里箭头的底轨会画成填充贴图（看起来像渲染了一块流体材质），进度显现也与普通配方相反。
 * <p>
 * 这里让流体配方改用普通箭头那一对贴图：底轨用 {@code progressRight[PROGRESS_ARROW]}，
 * 填充用 {@code progressRightFill[PROGRESS_ARROW]}，流体配方即与普通配方表现一致。
 * <p>
 * 部件贴图本身的淡灰底色改由覆写的 {@code thermal:textures/gui/jei_handler.png}（对齐 Mandala 深色主题）处理。
 */
@Mixin(value = Drawables.class, remap = false)
public abstract class DrawablesMixin {

    @Accessor("progressRight")
    protected abstract IDrawableStatic[] gt$progressRight();

    @Accessor("progressRightFill")
    protected abstract IDrawableStatic[] gt$progressRightFill();

    @ModifyReturnValue(method = "getProgress", at = @At("RETURN"))
    private IDrawableStatic gt$fluidProgressFill(IDrawableStatic original, @Local(argsOnly = true) int index) {
        return index == Drawables.PROGRESS_ARROW_FLUID
                ? gt$progressRightFill()[Drawables.PROGRESS_ARROW]
                : original;
    }

    @ModifyReturnValue(method = "getProgressFill", at = @At("RETURN"))
    private IDrawableStatic gt$fluidProgressBackground(IDrawableStatic original, @Local(argsOnly = true) int index) {
        return index == Drawables.PROGRESS_ARROW_FLUID
                ? gt$progressRight()[Drawables.PROGRESS_ARROW]
                : original;
    }
}
