package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.ThaumonomiconTabPaging;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.client.gui.ThaumonomiconData;
import thaumcraft.client.gui.ThaumonomiconScreen;

import java.util.List;

/**
 * 魔导手册分类标签分页（移植回 0.3.0）。
 *
 * <p>0.3.0 的标签布局仍由 {@code ThaumonomiconLayout.categoryTabX/Y(index)} 决定，每侧固定 9 个、
 * 共 18 个；分类超过 18 个时多出来的会画到书外。这里把交给
 * {@code ResearchMapView.drawCategoryTabs} 的分类列表换成当前页的切片，并在
 * {@code handleMapCategoryClick}（坐标已在标签坐标系）里拦截两侧箭头，实现翻页。</p>
 */
@Mixin(value = ThaumonomiconScreen.class, remap = false)
public abstract class ThaumonomiconScreenTabPagingMixin {

    @WrapOperation(
            method = {"renderResearchMap", "handleMapCategoryClick"},
            at = @At(value = "INVOKE", target = "Lthaumcraft/client/gui/ThaumonomiconScreen;bookCategories()Ljava/util/List;")
    )
    private List<ThaumonomiconData.Category> gt$pagedCategories(
            ThaumonomiconScreen screen, Operation<List<ThaumonomiconData.Category>> original) {
        return ThaumonomiconTabPaging.slice(original.call(screen));
    }

    @Inject(method = "handleMapCategoryClick", at = @At("HEAD"), cancellable = true)
    private void gt$handleTabPagingClick(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (!ThaumonomiconTabPaging.hasPages()) {
            return;
        }
        if (ThaumonomiconTabPaging.insidePrevious(mouseX, mouseY)) {
            ThaumonomiconTabPaging.setPage(ThaumonomiconTabPaging.page() - 1);
            cir.setReturnValue(true);
        } else if (ThaumonomiconTabPaging.insideNext(mouseX, mouseY)) {
            ThaumonomiconTabPaging.setPage(ThaumonomiconTabPaging.page() + 1);
            cir.setReturnValue(true);
        }
    }
}
