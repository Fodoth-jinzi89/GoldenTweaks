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
 * 「要素来源」页面中要素安瓿的物品单独偏移：槽位框保持 (64, 5) 不动，
 * 槽内物品向左上各移 2 像素。
 * <p>
 * TMRV 把 Thaumcraft 的 JEI 配方转成 EMI 渲染，槽位框与物品都由 EMI
 * {@link SlotWidget} 基于槽位矩形绘制。这里在 {@code drawStack} 中改写
 * 物品的实际渲染坐标，从而只移动物品、不动槽位框。
 */
@Mixin(SlotWidget.class)
public abstract class SlotWidgetMixin {

    /** 要素来源页输出槽（要素安瓿）的槽位位置。 */
    @Unique
    private static final int PHIAL_SLOT_X = 64;
    @Unique
    private static final int PHIAL_SLOT_Y = 5;
    /** 物品相对槽内居中位置的偏移。 */
    @Unique
    private static final int ITEM_OFFSET = 1;

    @Accessor("x")
    protected abstract int gt$x();

    @Accessor("y")
    protected abstract int gt$y();

    @Accessor("output")
    protected abstract boolean gt$output();

    @Accessor("drawBack")
    protected abstract boolean gt$drawBack();

    @Redirect(
            method = "drawStack",
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/stack/EmiIngredient;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
    )
    private void gt$shiftAspectPhial(EmiIngredient stack, GuiGraphics gui, int x, int y, float delta) {
        if (gt$x() == PHIAL_SLOT_X && gt$y() == PHIAL_SLOT_Y && gt$output() && gt$drawBack()) {
            stack.render(gui, x - ITEM_OFFSET, y - ITEM_OFFSET, delta);
        } else {
            stack.render(gui, x, y, delta);
        }
    }
}
