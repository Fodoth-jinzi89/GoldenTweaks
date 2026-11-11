package net.fodoth.skina.goldentweaks.mixin.fix.tmrv;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TMRV 把 JEI 配方页渲染进 EMI 时，整页由一个 {@code TMRVRecipeWidget} 宿主控件承载：
 * 它在 {@code mouseClicked} 里先把这次按键派发给 JEI 的 {@code IJeiGuiEventListener}
 * （{@code inputHandlers} / {@code guiEventListeners}），只要某一层返回 true 就整个吃掉这次按下。
 *
 * <p>问题在于它派发鼠标时用的是 {@code IJeiGuiEventListener.mouseReleased(...)}（真正 JEI 的
 * {@code RecipeLayoutInputHandler} 只在 EXECUTE 阶段调 {@code mouseReleased}，SIMULATE 阶段调的是
 * {@code mouseClicked}）。而 Mekanism 的 {@code BaseRecipeCategory#createRecipeExtras} 会给<b>每一个</b>
 * GUI 元素（流体/化学储罐、物品槽……）都挂一个 {@code MekJeiWidget}，它转调
 * {@code GuiElement.mouseReleased} → 原版 {@code AbstractWidget.mouseReleased}，后者对左键
 * 直接 {@code return true}。</p>
 *
 * <p>结果：左键点在储罐/物品槽上时宿主控件先返回 true，EMI {@code RecipeScreen} 就不会把这次按下
 * 记成 {@code pressedSlot}，松开时自然也不会调用 {@code SlotWidget.mouseClicked}，
 * 于是「点击查看配方/用途」完全没反应；而 R/U 走的是 EMI 全局的悬停取物路径，所以仍然正常。</p>
 *
 * <p>这里让宿主控件照旧派发事件，但不再把这次按下算作「已消费」，把点击交还给 EMI 自己的
 * SlotWidget（EMI 原生页面正是这么处理配方页点击的）。</p>
 */
@Mixin(targets = "dev.nolij.toomanyrecipeviewers.impl.recipe.TMRVRecipeWidget", remap = false)
public abstract class TmrvRecipeWidgetClickMixin {

    @Inject(method = "mouseClicked", at = @At("RETURN"), cancellable = true)
    private void gt$doNotSwallowClick(int mouseX, int mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
