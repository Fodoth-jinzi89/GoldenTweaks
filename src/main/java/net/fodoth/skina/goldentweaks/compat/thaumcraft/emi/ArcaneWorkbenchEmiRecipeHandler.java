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
import net.minecraft.world.entity.player.Inventory;
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
 *   <li>菜单槽 11~19：充能槽，<b>只有充能（升级）工作台才存在</b></li>
 *   <li>之后才是玩家背包 27 槽与快捷栏 9 槽</li>
 * </ul></p>
 *
 * <p>因为充能槽是条件添加的，玩家槽位的起始下标并不固定：普通工作台共 47 槽
 * （背包从菜单槽 11 开始），充能工作台共 56 槽（背包从菜单槽 20 开始）。因此这里
 * <b>不能写死下标</b>——按槽位归属（{@code slot.container instanceof Inventory}）筛选，
 * 既覆盖两种工作台，也不会再因为槽位数量变化而越界。可取材范围必须正好是玩家背包 +
 * 快捷栏：漏掉快捷栏会让材料放在快捷栏里的配方无法一键转移。</p>
 *
 * <p>转移/清空逻辑直接复用 EMI 内置的 {@code EmiRecipeFiller}（与原版工作台相同的
 * 默认实现），本类只需描述槽位关系。对任意配方均声明支持，因此奥术配方（经 EMI 的
 * JEI 兼容层显示）与原版配方都能一键转移。</p>
 */
public class ArcaneWorkbenchEmiRecipeHandler implements StandardRecipeHandler<ArcaneWorkbenchMenu> {

    @Override
    public List<Slot> getInputSources(ArcaneWorkbenchMenu menu) {
        List<Slot> slots = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (slot.container instanceof Inventory) {
                slots.add(slot);
            }
        }
        return slots;
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
        int from = Math.max(0, start);
        int to = Math.min(end, menu.slots.size());
        List<Slot> slots = new ArrayList<>(Math.max(0, to - from));
        for (int i = from; i < to; i++) {
            slots.add(menu.getSlot(i));
        }
        return slots;
    }
}
