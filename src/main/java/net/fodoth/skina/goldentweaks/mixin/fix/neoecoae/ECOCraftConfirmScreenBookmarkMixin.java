package net.fodoth.skina.goldentweaks.mixin.fix.neoecoae;

import appeng.api.stacks.AEKey;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import cn.dancingsnow.neoecoae.api.me.menu.ECOCraftConfirmMenuMode;
import cn.dancingsnow.neoecoae.client.gui.crafting.ECOCraftConfirmScreen;
import cn.dancingsnow.neoecoae.crafting.planner.snapshot.CraftingGraphSnapshot;
import cn.dancingsnow.neoecoae.crafting.planner.snapshot.CraftingGraphSnapshot.MaterialNode;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiFavorites;
import dev.emi.emi.screen.EmiScreenManager;
import net.fodoth.skina.goldentweaks.compat.ae2.GTAe2EmiStacks;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * neoecoae 合成报告界面（{@link ECOCraftConfirmScreen}）底部那颗「收藏缺失」按钮走的是
 * {@code JeiBookmarkAccess}：用反射去拿 JEI 的 {@code BookmarkOverlay#bookmarkList} /
 * {@code BookmarkFactory} 再往里塞书签。本整合包只装了 EMI（没有 JEI），这些 JEI 内部类根本不存在，
 * 于是点击后反射一路抛异常被吞掉，按钮看起来完全无效。
 *
 * <p>这里在点击发生时改接 EMI 自己的收藏夹：优先取 AE2 合成计划里「缺失数量 &gt; 0」的条目
 * （ECO 快照在走 AE2 原生规划器时可能为空，那时只按快照会一个都收藏不到），用
 * {@link GTAe2EmiStacks} 转成 {@link EmiStack} 后加入 {@link EmiFavorites}，再刷新收藏夹侧栏并取消原方法。
 * 同时把按钮在「计划确有缺失」时强制置为可用，避免依赖 ECO 快照导致按钮变灰点不动。</p>
 */
@Mixin(value = ECOCraftConfirmScreen.class, remap = false)
public abstract class ECOCraftConfirmScreenBookmarkMixin {

    @Shadow
    @Final
    private Button bookmarkMissing;

    @Inject(method = "updateBeforeRender()V", at = @At("TAIL"))
    private void goldentweaks$enableBookmarkWhenPlanMissing(CallbackInfo ci) {
        if (this.bookmarkMissing == null || this.bookmarkMissing.active) {
            return;
        }
        CraftingPlanSummary plan = ((ECOCraftConfirmScreen) (Object) this).getMenu().getPlan();
        if (plan != null && plan.getEntries().stream().anyMatch(entry -> entry.getMissingAmount() > 0L)) {
            this.bookmarkMissing.active = true;
        }
    }

    @Inject(method = "bookmarkMissing()V", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$favoriteMissingIntoEmi(CallbackInfo ci) {
        CraftConfirmMenu menu = ((ECOCraftConfirmScreen) (Object) this).getMenu();
        List<AEKey> missing = collectMissing(menu);

        int added = 0;
        for (AEKey key : missing) {
            EmiStack stack = GTAe2EmiStacks.toEmiStack(key);
            if (stack != null && !stack.isEmpty()) {
                EmiFavorites.addFavorite(stack);
                added++;
            }
        }
        if (added > 0) {
            EmiScreenManager.repopulatePanels(SidebarType.FAVORITES);
        }
        ci.cancel();
    }

    private static List<AEKey> collectMissing(CraftConfirmMenu menu) {
        List<AEKey> missing = new ArrayList<>();
        CraftingPlanSummary plan = menu.getPlan();
        if (plan != null) {
            for (CraftingPlanSummaryEntry entry : plan.getEntries()) {
                if (entry.getMissingAmount() > 0L && entry.getWhat() != null) {
                    missing.add(entry.getWhat());
                }
            }
        }
        if (missing.isEmpty() && menu instanceof ECOCraftConfirmMenuMode mode) {
            CraftingGraphSnapshot snapshot = mode.neoecoae$getCraftingGraphSnapshot();
            if (snapshot != null) {
                for (MaterialNode node : snapshot.nodes()) {
                    if (node.missingBigInteger().signum() > 0 && node.key() != null) {
                        missing.add(node.key());
                    }
                }
            }
        }
        return missing;
    }
}
