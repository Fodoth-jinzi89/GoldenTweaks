package net.fodoth.skina.goldentweaks.event;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiHistory;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import net.fodoth.skina.goldentweaks.compat.ftbquests.client.GTQuestImageHover;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * 任务书界面的 R/U 查配方：直接把「鼠标下的 ingredient」交给 EMI。
 *
 * <p>FTB Library 的 R/U 本来靠 {@code JEIIntegration} 这个全局 GUI 处理器把 ingredient 转成 JEI 类型，
 * 再由 TMRV 转成 EMI 的栈 —— 本包用 TMRV 顶替 JEI 之后，这条链路在任务书里没打通
 * （悬停 tooltip 是正常的，说明 {@code getIngredientUnderMouse()} 本身没问题，断的是后面那段）。</p>
 *
 * <p>这里干脆绕过 JEI：按键时若当前是 FTB Library 的界面，取下
 * {@code getGui().getIngredientUnderMouse()}，命中 EMI 的「查看配方 / 查看用途」绑定就直接调
 * {@link EmiApi#displayRecipes} / {@link EmiApi#displayUses}。任务条目、奖励图标、章节图片等
 * 所有走 {@code PositionedIngredient} 的控件因此都能 R/U。</p>
 *
 * <p>本类的方法签名引用 EMI 类型，因此<strong>不能</strong>用 {@code @EventBusSubscriber} 自动注册：
 * 注解扫描会解析这些签名，未安装 EMI 时会在启动阶段抛 {@code NoClassDefFoundError}。
 * 改由 {@link net.fodoth.skina.goldentweaks.GoldenTweaks} 在客户端启动且 EMI + FTB Quests 均存在时手动注册。</p>
 */
public final class QuestBookRecipeKeyHandler {

    private QuestBookRecipeKeyHandler() {
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!ModList.get().isLoaded("emi") || !ModList.get().isLoaded("ftbquests")) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof IScreenWrapper wrapper)) {
            return;
        }

        Object ingredient = wrapper.getGui().getIngredientUnderMouse()
                .map(PositionedIngredient::ingredient)
                .orElse(null);

        EmiStack stack = toEmiStack(ingredient);
        if (stack == null) {
            return;
        }

        Screen before = minecraft.screen;
        boolean handled;
        if (EmiConfig.viewRecipes != null && EmiConfig.viewRecipes.matchesKey(event.getKeyCode(), event.getScanCode())) {
            open(GTQuestImageHover.recipeIdFor(ingredient), stack, true);
            handled = true;
        } else if (EmiConfig.viewUses != null && EmiConfig.viewUses.matchesKey(event.getKeyCode(), event.getScanCode())) {
            open(GTQuestImageHover.usesIdFor(ingredient), stack, false);
            handled = true;
        } else {
            handled = false;
        }

        if (!handled) {
            return;
        }
        event.setCanceled(true);

        // EMI 的 RecipeScreen 构造参数是 AbstractContainerScreen（只支持从容器的界面进入），
        // 遇到任务书这种非容器界面时它会先把屏幕换成玩家背包，于是 E/Esc 会退回背包。
        // EmiHistory 存的是普通 Screen，这里把「返回目标」补上原界面，关闭时就回到任务书。
        if (before != null && minecraft.screen != before) {
            EmiHistory.push(before);
        }
    }

    /**
     * 图片上绑定了配方 id 且能在 EMI 里找到时直接跳那个配方，否则退回「按这个物品列配方 / 列用途」。
     */
    private static void open(String boundRecipeId, EmiStack stack, boolean recipes) {
        EmiRecipe bound = findRecipe(boundRecipeId);
        if (bound != null) {
            EmiApi.displayRecipe(bound);
            return;
        }
        if (recipes) {
            EmiApi.displayRecipes(stack);
        } else {
            EmiApi.displayUses(stack);
        }
    }

    private static EmiRecipe findRecipe(String boundRecipeId) {
        if (boundRecipeId == null || boundRecipeId.isEmpty()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(boundRecipeId);
        return id == null ? null : EmiApi.getRecipeManager().getRecipe(id);
    }

    private static EmiStack toEmiStack(Object ingredient) {
        if (ingredient instanceof ItemStack item && !item.isEmpty()) {
            return EmiStack.of(item);
        }
        if (ingredient instanceof FluidStack fluid && !fluid.isEmpty()) {
            return EmiStack.of(fluid.getFluid(), fluid.getAmount());
        }
        return null;
    }
}
