package net.fodoth.skina.goldentweaks.mixin.fix.thermal;

import cofh.thermal.lib.compat.jei.Drawables;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Thermal 的 JEI 部件（进度箭头、能量/冷却/速度量表、格子）用的是它自己 {@code jei_handler.png}
 * 里的贴图，风格和 JEI 标准部件不一致，在 Mandala 这类只重绘 JEI 标准贴图的 GUI 资源包里会显得很突兀。
 * <p>
 * 这里把 {@link Drawables} 的这些 getter 换成 JEI 标准绘制器（{@code IGuiHelper} 的箭头/火焰/格子），
 * 于是 Thermal 的配方部件就会跟着 JEI 的标准贴图走 —— 也就自动适配 Mandala 的 UI。
 */
@Mixin(value = Drawables.class, remap = false)
public abstract class DrawablesMixin {

    @Unique
    private IGuiHelper gt$helper;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gt$captureHelper(IGuiHelper helper, CallbackInfo ci) {
        this.gt$helper = helper;
    }

    @ModifyReturnValue(method = "getSlot", at = @At("RETURN"))
    private IDrawableStatic gt$standardSlot(IDrawableStatic original) {
        return gt$helper == null ? original : gt$helper.getSlotDrawable();
    }

    @ModifyReturnValue(method = {"getProgress", "getProgressLeft"}, at = @At("RETURN"))
    private IDrawableStatic gt$standardArrow(IDrawableStatic original) {
        return gt$helper == null ? original : gt$helper.getRecipeArrow();
    }

    @ModifyReturnValue(method = {"getProgressFill", "getProgressLeftFill"}, at = @At("RETURN"))
    private IDrawableStatic gt$standardArrowFill(IDrawableStatic original) {
        return gt$helper == null ? original : gt$helper.getRecipeArrowFilled();
    }

    @ModifyReturnValue(method = {"getEnergyEmpty", "getCoolantEmpty"}, at = @At("RETURN"))
    private IDrawableStatic gt$standardFlameEmpty(IDrawableStatic original) {
        return gt$helper == null ? original : gt$helper.getRecipeFlameEmpty();
    }

    @ModifyReturnValue(method = {"getEnergyFill", "getCoolantFill"}, at = @At("RETURN"))
    private IDrawableStatic gt$standardFlameFill(IDrawableStatic original) {
        return gt$helper == null ? original : gt$helper.getRecipeFlameFilled();
    }
}
