package net.fodoth.skina.goldentweaks.compat.thaumcraft.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.world.inventory.Slot;
import thaumcraft.common.menu.ArcaneWorkbenchMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * 奥术工作台的 EMI 配方转移处理器（移植自 TC4Tweaks 时代的便利功能：工作台支持
 * "+ 号按钮" 自动把配方材料转移进 3x3 合成网格）。
 *
 * <p>槽位布局（与 {@link ArcaneWorkbenchMenu} 构造器一致）：
 * <ul>
 *   <li>菜单槽 0：结果槽</li>
 *   <li>菜单槽 1：法杖槽</li>
 *   <li>菜单槽 2~10：3x3 合成网格（对应容器槽 0~8）</li>
 *   <li>菜单槽 11~46：玩家背包 + 快捷栏</li>
 * </ul></p>
 *
 * <p>转移/清空逻辑直接复用 EMI 内置的 {@code EmiRecipeFiller}（与原版工作台相同的
 * 默认实现），本类只需描述槽位关系。对任意配方均声明支持，因此奥术配方（经 EMI 的
 * JEI 兼容层显示）与原版配方都能一键转移。</p>
 */
public class ArcaneWorkbenchEmiRecipeHandler implements StandardRecipeHandler<ArcaneWorkbenchMenu> {

    @Override
    public List<Slot> getInputSources(ArcaneWorkbenchMenu menu) {
        return slotRange(menu, 11, 47);
    }

    @Override
    public List<Slot> getCraftingSlots(ArcaneWorkbenchMenu menu) {
        return slotRange(menu, 2, 11);
    }

    @Override
    public Slot getOutputSlot(ArcaneWorkbenchMenu menu) {
        return menu.getSlot(0);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return true;
    }

    private static List<Slot> slotRange(ArcaneWorkbenchMenu menu, int start, int end) {
        List<Slot> slots = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) {
            slots.add(menu.getSlot(i));
        }
        return slots;
    }
}
