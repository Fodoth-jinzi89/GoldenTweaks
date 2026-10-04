package net.fodoth.skina.goldentweaks.mixin.feature.ftbquests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import net.fodoth.skina.goldentweaks.compat.ftbquests.client.GTQuestImageHover;
import net.fodoth.skina.goldentweaks.compat.ftbquests.client.GTQuestImageItems;
import net.fodoth.skina.goldentweaks.compat.ftbquests.client.GTQuestImageRecipeBinding;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * 任务书里的「物品图片」就是 {@code ChapterImage}（章节画布上的一张图，带 {@code dependency} 前置任务）。
 * FTB 原版把它当成一张扁平贴图来画，本包更是把物品/流体写成了贴图路径，于是：
 *
 * <ol>
 *     <li><b>解锁后不立即出现</b>：{@code QuestPanel#addWidgets} 只在「画布重建」时按
 *     {@code shouldShowImage} 过滤并创建 {@code ChapterImageButton}，前置任务完成时画布不会重建，
 *     图片要等切一次编辑模式才出现。配合 {@link QuestPanelImageWidgetsMixin} 让控件始终创建，
 *     这里在 {@code draw} 开头把「还没解锁」的图直接跳过，解锁后下一帧就能显示。</li>
 *     <li><b>只画一张图 / 方块变灰</b>：贴图路径能还原成物品时（见 {@link GTQuestImageItems}）
 *     改为渲染真正的 ItemStack。注意不能直接用 {@code ItemIcon.draw(0,0,1,1)}：FTB 画这张图时
 *     已经 {@code pose.scale(w,h,1)} 了，{@code ItemIcon} 内部再 {@code scale(1/16)}，
 *     合起来 z 轴只缩了 1/16 而 x/y 缩了 w/16 —— 方块的立体模型被压扁，光照就变成一层灰。
 *     所以这里先把这层非等比缩放抵消掉（保留旋转），再按像素尺寸画。</li>
 *     <li><b>悬浮提示 / R/U 查配方</b>：{@code getIngredientUnderMouse()} 返回解析出的物品/流体，
 *     FTB Library 据此渲染默认 tooltip，JEI（本包由 TMRV→EMI 提供）据此做 R/U；
 *     另外直接往 {@code addMouseOverText} 里塞 tooltip，免得依赖 FTB 那条「tooltip 列表为空才画物品
 *     tooltip」的分支；{@code checkMouseOver} 里原本 {@code getClickAction().isNone() && !canEdit()}
 *     会把「不可点击的图片」判成不可悬停，这里在图片确实可见且能解析出物品/流体时放行。</li>
 * </ol>
 */
@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton", remap = false)
public abstract class ChapterImageButtonItemMixin {

    @Shadow
    private ChapterImage chapterImage;

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true, remap = false)
    private void gt$hideUntilUnlocked(GuiGraphics graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null || file.canEdit()) {
            return;
        }
        if (!this.chapterImage.shouldShowImage(file.selfTeamData)) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ftb/mods/ftblibrary/icon/Icon;draw(Lnet/minecraft/client/gui/GuiGraphics;IIII)V"
            ),
            remap = false
    )
    private void gt$drawRealItemStack(Icon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        ItemStack stack = GTQuestImageItems.resolveItem(this.chapterImage.getImage());
        if (stack.isEmpty()) {
            icon.draw(graphics, x, y, w, h);
            return;
        }

        // 这张图在控件里占据 width x height 像素，且当前 pose 已经被 FTB 按 (width, height) 非等比缩放
        Widget self = (Widget) (Object) this;
        int boxWidth = self.getWidth();
        int boxHeight = self.getHeight();
        int size = Math.min(boxWidth, boxHeight);
        if (size <= 0) {
            icon.draw(graphics, x, y, w, h);
            return;
        }

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale(1.0F / boxWidth, 1.0F / boxHeight, 1.0F);
        ItemIcon.getItemIcon(stack).draw(graphics, (boxWidth - size) / 2, (boxHeight - size) / 2, size, size);
        pose.popPose();
    }

    @Inject(method = "addMouseOverText", at = @At("HEAD"), remap = false)
    private void gt$addFluidTooltip(TooltipList list, CallbackInfo ci) {
        Icon icon = this.chapterImage.getImage();

        // 物品不在这里加 tooltip：留给 FTB Library 走「ingredient tooltip」那条分支，
        // 也就是 graphics.renderTooltip(Font, ItemStack, …) —— 这样才是真正的物品 tooltip，
        // 而且 EMI 的 DrawContextMixin 只认这个重载，认到才知道鼠标下是哪个物品（R/U 用）。
        if (!GTQuestImageItems.resolveItem(icon).isEmpty()) {
            return;
        }

        FluidStack fluid = GTQuestImageItems.resolveFluid(icon);
        if (!fluid.isEmpty()) {
            list.add(fluid.getHoverName());
        }
    }

    @Redirect(
            method = "checkMouseOver",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/client/ClientQuestFile;canEdit()Z"
            ),
            remap = false
    )
    private boolean gt$hoverableIngredientImage(ClientQuestFile file) {
        if (file.canEdit()) {
            return true;
        }
        if (!this.chapterImage.shouldShowImage(file.selfTeamData)) {
            return false;
        }

        Icon icon = this.chapterImage.getImage();
        return !GTQuestImageItems.resolveItem(icon).isEmpty() || !GTQuestImageItems.resolveFluid(icon).isEmpty();
    }

    public Optional<PositionedIngredient> getIngredientUnderMouse() {
        Icon icon = this.chapterImage.getImage();

        ItemStack stack = GTQuestImageItems.resolveItem(icon);
        if (!stack.isEmpty()) {
            GTQuestImageHover.remember(stack, FluidStack.EMPTY, this.gt$boundRecipeId(), this.gt$boundUsesId());
            return PositionedIngredient.of(stack, (Widget) (Object) this, true);
        }

        FluidStack fluid = GTQuestImageItems.resolveFluid(icon);
        if (!fluid.isEmpty()) {
            GTQuestImageHover.remember(ItemStack.EMPTY, fluid, this.gt$boundRecipeId(), this.gt$boundUsesId());
            return PositionedIngredient.of(fluid, (Widget) (Object) this, true);
        }

        return PositionedIngredient.of(icon.getIngredient(), (Widget) (Object) this, true);
    }

    private String gt$boundRecipeId() {
        // ChapterImage 是 final 类，直接 instanceof 会被编译器判定为永假，所以先赋给 Object 变量
        Object chapterImage = this.chapterImage;
        return chapterImage instanceof GTQuestImageRecipeBinding binding
                ? binding.goldentweaks$imageRecipeId() : "";
    }

    private String gt$boundUsesId() {
        Object chapterImage = this.chapterImage;
        return chapterImage instanceof GTQuestImageRecipeBinding binding
                ? binding.goldentweaks$imageUsesId() : "";
    }
}
