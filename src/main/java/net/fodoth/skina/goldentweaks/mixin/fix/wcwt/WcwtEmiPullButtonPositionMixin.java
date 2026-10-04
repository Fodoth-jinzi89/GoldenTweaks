package net.fodoth.skina.goldentweaks.mixin.fix.wcwt;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.lhy.wcwt.compat.emi.WcwtEmiPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 修正 wcwt「填充物品」锤子按钮在 EMI 配方页右侧按钮列里的落点。
 * <p>
 * EMI 的 {@code RecipeDisplay#addButtons} 会把右侧按钮（合成时为「+ 填充」「配方树」「设为默认配方」）
 * 从 {@code bottom} 起每 14 像素向上一格排成同一列，最上面一个是「设为默认配方」。
 * wcwt 的 {@code WcwtEmiPlugin#pullButtonPos} 想复刻这套布局，但在三个按钮的情形下
 * （{@code rightButtons == 3}）算成了 {@code {x + 14, bottom}}，也就是跑到右边另开一列，
 * 而不是叠在该列最上方按钮之上。
 * <p>
 * 这里把起始位置改为 EMI 右侧按钮列的<b>第一格</b>（{@code x = 显示宽度 + 5、y = bottom}，正是
 * 「+」按钮所在格），再交给 wcwt 自己的
 * {@code WcwtEmiPullItemsWidget#resolvePlacement} 逐格上移避让：它会依次跳过「+」「配方树」
 * 「设为默认配方」，最终停在最上方按钮之上；该 widget 持的是 widget group 的实时列表，
 * 因此能正确看到这些按钮。按钮换列（{@code rows < buttons}）时保持 wcwt 原逻辑。
 */
@Mixin(value = WcwtEmiPlugin.class, remap = false)
public abstract class WcwtEmiPullButtonPositionMixin {

    @ModifyReturnValue(method = "pullButtonPos", at = @At("RETURN"))
    private static int[] goldentweaks$alignWithEmiButtonColumn(int[] original, EmiRecipe recipe) {
        int height = recipe.getDisplayHeight();
        int rows = Math.max(1, (height + 10) / 14);
        int buttons = recipe.supportsRecipeTree() ? 3 : 1;
        if (rows < buttons) {
            return original;
        }

        int space = Math.min(8, height + 8 - (buttons * 14 - 2));
        int bottom = height + 4 - 12 - space / 2;
        return new int[]{recipe.getDisplayWidth() + 5, bottom};
    }
}
