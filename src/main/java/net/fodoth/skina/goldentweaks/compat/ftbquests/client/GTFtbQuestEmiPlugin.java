package net.fodoth.skina.goldentweaks.compat.ftbquests.client;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * 让 EMI 认得出 FTB Quest 界面里「鼠标底下那个物品/流体」，从而 R/U 能查配方。
 *
 * <p>FTB Quest 的 R/U 本来是走 JEI 的全局 GUI 处理器（FTB Library 的
 * {@code JEIIntegration#getClickableIngredientUnderMouse} → {@code ClientQuestFile} 里
 * 悬停控件提供的 {@code PositionedIngredient}）。本包用 TMRV 顶替 JEI，这条链路对任务条目能用，
 * 但对章节图片这种「控件自己提供 ingredient」的情况会断在中间，于是图片有 tooltip 却按 R/U 没反应。</p>
 *
 * <p>这里直接注册一个 EMI 的通用 stack provider：把 FTB 已经算好的
 * {@code getIngredientUnderMouse()} 转成 EMI 能吃的 {@link EmiStack}。EMI 是「取第一个非空结果」，
 * TMRV 那个 provider 排在前面、能返回时就轮不到这里，所以只在上面那条链路失效时兜底，不会抢占
 * 任务条目本来正常的 R/U。</p>
 */
@EmiEntrypoint
public class GTFtbQuestEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        if (!ModList.get().isLoaded("ftbquests")) {
            return;
        }
        registry.addGenericStackProvider(GTFtbQuestEmiPlugin::provideStackAt);
    }

    private static EmiStackInteraction provideStackAt(Screen screen, int mouseX, int mouseY) {
        if (!(screen instanceof IScreenWrapper wrapper)) {
            return EmiStackInteraction.EMPTY;
        }

        Object ingredient = wrapper.getGui().getIngredientUnderMouse()
                .map(PositionedIngredient::ingredient)
                .orElse(null);

        if (ingredient instanceof ItemStack stack && !stack.isEmpty()) {
            return new EmiStackInteraction(EmiStack.of(stack), null, false);
        }
        if (ingredient instanceof FluidStack fluid && !fluid.isEmpty()) {
            return new EmiStackInteraction(EmiStack.of(fluid.getFluid(), fluid.getAmount()), null, false);
        }
        return EmiStackInteraction.EMPTY;
    }
}
