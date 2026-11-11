package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.ThaumonomiconAspectSources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.gui.AspectGuiRenderer;
import thaumcraft.client.gui.thaumonomicon.ResearchPageRenderer;
import thaumcraft.common.research.ResearchAspectPageLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 要素知识页的旧版排布：每页 10 个，**两列 × 五行**，按「等级（primal=1，合成物逐级 +1）」排序，
 * 左上角标等级数字，名称居中在图标下方，合成物显示为「成分 + 成分」并带名字。
 *
 * <p>0.3.0 自带的是单列、每行 50px，一页 10 个会直接画出页外。这里在
 * {@code drawAspectKnowledgePage} 入口整段接管（分页仍由
 * {@code ResearchAspectPageLayoutMixin} 保证每页 10 个）。</p>
 */
@Mixin(value = ResearchPageRenderer.class, remap = false)
public abstract class AspectKnowledgeLayoutMixin {

    @Unique
    private static final int GT_COLUMN_STEP = 84;
    @Unique
    private static final int GT_ROW_STEP = 48;
    @Unique
    private static final int GT_ASPECT_SIZE = 32;
    @Unique
    private static final int GT_COMPONENT_SIZE = 17;
    @Unique
    private static final float GT_COMPONENT_NAME_SCALE = 0.75F;
    @Unique
    private static final int GT_NAME_COLOR = 0x507060;
    @Unique
    private static final int GT_COMPONENT_COLOR = 0xAAAAAA;
    @Unique
    private static final int GT_PRIMAL_COLOR = 0x507060;

    @Shadow
    private int mouseX;

    @Shadow
    private int mouseY;

    @Shadow
    protected abstract void drawLargeAspectTag(GuiGraphics graphics, Aspect aspect, int amount, int x, int y);

    @Inject(method = "drawAspectKnowledgePage", at = @At("HEAD"), cancellable = true)
    private void gt$drawAspectKnowledgePage(GuiGraphics graphics, ResearchAspectPageLayout.Page page,
                                            int x, int y, int width, Player player, CallbackInfo ci) {
        if (page == null) {
            return;
        }
        List<ResearchAspectPageLayout.Entry> entries = new ArrayList<>(page.entries());
        entries.sort(Comparator.comparingInt((ResearchAspectPageLayout.Entry entry) -> gt$tier(entry.aspect()))
                .thenComparing(entry -> entry.aspect().tag()));

        Font font = Minecraft.getInstance().font;
        Aspect hovered = null;
        int hoveredY = 0;

        for (int i = 0; i < entries.size(); i++) {
            ResearchAspectPageLayout.Entry entry = entries.get(i);
            Aspect aspect = entry.aspect();
            int ex = x + (i % 2) * GT_COLUMN_STEP;
            int ey = y + (i / 2) * GT_ROW_STEP;

            drawLargeAspectTag(graphics, aspect, entry.amount(), ex, ey);

            String tier = Integer.toString(gt$tier(aspect));
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 200.0F);
            graphics.drawString(font, tier, ex + 2, ey + 2, 0xFFFFFF, true);
            graphics.pose().popPose();

            String name = aspect.displayName().getString();
            graphics.drawString(font, name, ex + GT_ASPECT_SIZE / 2 - font.width(name) / 2, ey + 36,
                    GT_NAME_COLOR, false);

            if (entry.primal()) {
                String primal = Component.translatable("tc.aspect.primal").getString();
                graphics.drawString(font, primal, ex + 56 - font.width(primal) / 2, ey + 12,
                        GT_PRIMAL_COLOR, false);
            } else if (entry.components().size() >= 2) {
                Aspect first = entry.components().get(0);
                Aspect second = entry.components().get(1);
                AspectGuiRenderer.draw(graphics, first, ex + 36, ey + 4, GT_COMPONENT_SIZE, 1.0F);
                graphics.drawString(font, "+", ex + 56, ey + 14, GT_COMPONENT_COLOR, false);
                AspectGuiRenderer.draw(graphics, second, ex + 64, ey + 4, GT_COMPONENT_SIZE, 1.0F);
                gt$drawComponentName(graphics, font, first, ex + 44, ey + 30);
                gt$drawComponentName(graphics, font, second, ex + 72, ey + 30);
            }

            if (this.mouseX >= ex + 2 && this.mouseX < ex + 2 + GT_ASPECT_SIZE - 4
                    && this.mouseY >= ey + 2 && this.mouseY < ey + 2 + GT_ASPECT_SIZE - 4) {
                hovered = aspect;
                hoveredY = ey;
            }
        }

        if (hovered != null) {
            ThaumonomiconAspectSources.render(graphics, hovered, x, hoveredY, width, player);
        }
        ci.cancel();
    }

    @Unique
    private static void gt$drawComponentName(GuiGraphics graphics, Font font, Aspect aspect, int centerX, int y) {
        String name = aspect.displayName().getString();
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, y, 0.0F);
        graphics.pose().scale(GT_COMPONENT_NAME_SCALE, GT_COMPONENT_NAME_SCALE, 1.0F);
        graphics.drawString(font, name, -font.width(name) / 2, 0, GT_NAME_COLOR, false);
        graphics.pose().popPose();
    }

    @Unique
    private static int gt$tier(Aspect aspect) {
        if (aspect.isPrimal()) {
            return 1;
        }
        int max = 1;
        for (Aspect component : aspect.components()) {
            max = Math.max(max, gt$tier(component));
        }
        return max + 1;
    }
}
