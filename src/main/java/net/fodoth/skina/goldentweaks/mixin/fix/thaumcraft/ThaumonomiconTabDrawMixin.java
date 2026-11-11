package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.ThaumonomiconTabPaging;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.client.gui.ThaumonomiconData;
import thaumcraft.client.gui.thaumonomicon.ResearchMapView;

import java.util.List;

/** 在分类标签列下面补画分页箭头（{@link ThaumonomiconTabPaging}）。 */
@Mixin(value = ResearchMapView.class, remap = false)
public abstract class ThaumonomiconTabDrawMixin {

    @Inject(method = "drawCategoryTabs", at = @At("RETURN"))
    private static void gt$drawTabPagingArrows(GuiGraphics graphics, List<ThaumonomiconData.Category> categories,
                                               String selectedKey, int mouseX, int mouseY,
                                               CallbackInfoReturnable<ThaumonomiconData.Category> cir) {
        ThaumonomiconTabPaging.drawArrows(graphics);
    }
}
