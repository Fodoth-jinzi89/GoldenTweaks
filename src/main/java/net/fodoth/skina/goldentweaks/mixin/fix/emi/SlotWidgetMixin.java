package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 若干页面里输出槽内的物品/要素需要相对槽位框单独向左上偏移 1 像素（框不动）。
 * <p>
 * TMRV 把 JEI 配方转成 EMI 渲染，槽位框与物品都由 EMI {@link SlotWidget} 基于槽位矩形绘制。
 * 这里在 {@code drawStack} 中改写物品的实际渲染坐标，从而只移动物品、不动槽位框。已覆盖：
 * <ul>
 *     <li>神秘时代「要素来源」要素安瓿槽 (124, 6)</li>
 *     <li>AcademyCraft 幻想融合机 (90, 16)、金属成型机 (90, 13)</li>
 * </ul>
 *
 * <p><b>迁移说明：</b>「要素来源」页原先来自独立模组 {@code scex_aspect_jei}，其输出槽在
 * {@code (127, 20)}；该页已并入神秘时代本体
 * （{@code thaumcraft.integration.jei.AspectSourceRecipeCategory}），
 * 槽位改为 {@code (124, 6)}——{@code setRecipe} 里 {@code addSlot(...)} 的两个 bipush
 * 就是这对坐标。旧的 {@code (64, 5)} 是更早期布局，已不再可能出现。</p>
 */
@Mixin(SlotWidget.class)
public abstract class SlotWidgetMixin {

    /** 物品相对槽内居中位置的偏移。 */
    @Unique
    private static final int ITEM_OFFSET = 1;

    @Accessor("x")
    protected abstract int gt$x();

    @Accessor("y")
    protected abstract int gt$y();

    @Accessor("output")
    protected abstract boolean gt$output();

    @Unique
    private static boolean gt$isShiftedOutputSlot(int x, int y) {
        // 神秘时代「要素来源」的输出槽（并入本体后的坐标）。
        if (x == 124 && y == 6) {
            return true;
        }
        // AcademyCraft 幻想融合机 / 金属成型机。
        return x == 90 && (y == 13 || y == 16);
    }

    @Redirect(
            method = "drawStack",
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/stack/EmiIngredient;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
    )
    private void gt$shiftOutputItem(EmiIngredient stack, GuiGraphics gui, int x, int y, float delta) {
        if (gt$output() && gt$isShiftedOutputSlot(gt$x(), gt$y())) {
            stack.render(gui, x - ITEM_OFFSET, y - ITEM_OFFSET, delta);
        } else {
            stack.render(gui, x, y, delta);
        }
    }
}
