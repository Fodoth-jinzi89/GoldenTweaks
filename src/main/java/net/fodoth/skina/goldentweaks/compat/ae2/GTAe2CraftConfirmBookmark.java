package net.fodoth.skina.goldentweaks.compat.ae2;

import appeng.client.gui.AEBaseScreen;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiFavorites;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;

/**
 * 「收藏缺失」的共用落地逻辑：把当前合成计划里「缺失数量 &gt; 0」的材料加进 EMI 收藏夹并刷新侧栏。
 *
 * <p>AEQC 与 neoecoae 的对应按钮原本都走 JEI 内部类（本包的 JEI 是 TMRV 的桩，只有 API、
 * 没有那些内部类），在纯 EMI 环境里点了没反应；两边的补丁都改到这里。</p>
 */
public final class GTAe2CraftConfirmBookmark {

    private GTAe2CraftConfirmBookmark() {
    }

    public static void favoriteMissingIntoEmi() {
        if (!(Minecraft.getInstance().screen instanceof AEBaseScreen<?> screen)
                || !(screen.getMenu() instanceof CraftConfirmMenu menu)) {
            return;
        }

        CraftingPlanSummary plan = menu.getPlan();
        if (plan == null) {
            return;
        }

        boolean added = false;
        for (CraftingPlanSummaryEntry entry : plan.getEntries()) {
            if (entry.getMissingAmount() <= 0L || entry.getWhat() == null) {
                continue;
            }
            EmiStack stack = GTAe2EmiStacks.toEmiStack(entry.getWhat());
            if (stack != null && !stack.isEmpty()) {
                EmiFavorites.addFavorite(stack);
                added = true;
            }
        }
        if (added) {
            EmiScreenManager.repopulatePanels(SidebarType.FAVORITES);
        }
    }
}
