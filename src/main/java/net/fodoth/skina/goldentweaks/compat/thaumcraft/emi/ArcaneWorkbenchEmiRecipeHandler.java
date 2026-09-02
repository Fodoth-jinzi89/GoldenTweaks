package net.fodoth.skina.goldentweaks.compat.thaumcraft.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
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

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ArcaneWorkbenchMenu> context) {
        return StandardRecipeHandler.super.canCraft(itemOnly(recipe), context);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<ArcaneWorkbenchMenu> context) {
        return StandardRecipeHandler.super.craft(itemOnly(recipe), context);
    }

    private static EmiRecipe itemOnly(EmiRecipe recipe) {
        boolean hasNonItemInput = recipe.getInputs().stream()
                .anyMatch(input -> input.getEmiStacks().stream()
                        .noneMatch(stack -> {
                            ItemStack item = stack.getItemStack();
                            return item != null && !item.isEmpty();
                        }));
        List<EmiIngredient> inputs = recipe.getInputs().stream()
                .map(input -> input.getEmiStacks().stream().anyMatch(stack -> {
                    ItemStack item = stack.getItemStack();
                    return item != null && !item.isEmpty();
                }) || input.getEmiStacks().stream().anyMatch(stack -> stack.getKey() instanceof TagKey<?> tag
                        && tag.isFor(BuiltInRegistries.ITEM.key())) ? input : EmiStack.EMPTY)
                .limit(9)
                .toList();
        if (!hasNonItemInput) {
            return recipe;
        }
        return new EmiRecipe() {
            @Override public EmiRecipeCategory getCategory() { return recipe.getCategory(); }
            @Override public ResourceLocation getId() { return recipe.getId(); }
            @Override public List<EmiIngredient> getInputs() { return inputs; }
            @Override public List<EmiStack> getOutputs() { return recipe.getOutputs(); }
            @Override public int getDisplayWidth() { return recipe.getDisplayWidth(); }
            @Override public int getDisplayHeight() { return recipe.getDisplayHeight(); }
            @Override public void addWidgets(WidgetHolder widgets) { recipe.addWidgets(widgets); }
        };
    }

    private static List<Slot> slotRange(ArcaneWorkbenchMenu menu, int start, int end) {
        List<Slot> slots = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) {
            slots.add(menu.getSlot(i));
        }
        return slots;
    }
}
