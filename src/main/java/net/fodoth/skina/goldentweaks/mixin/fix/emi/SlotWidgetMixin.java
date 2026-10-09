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
 *     <li>神秘时代「要素来源」要素安瓿槽 (64, 5)</li>
 *     <li>scex_aspect_jei 要素组合 (127, 20)</li>
 *     <li>AcademyCraft 幻想融合机 (90, 16)、金属成型机 (90, 13)</li>
 * </ul>
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
        if (x == 64 && y == 5) {
            return true;
        }
        if (x == 127 && y == 20) {
            return true;
        }
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
