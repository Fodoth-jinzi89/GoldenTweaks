package net.fodoth.skina.goldentweaks.compat.ae2;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.StackWithBounds;
import appeng.menu.me.crafting.CraftConfirmMenu;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;

/**
 * 让 EMI 在 AE2 的合成计划界面上重新可用。
 *
 * <p>合成计划界面有两套：AE2 原版 {@code CraftConfirmScreen}，以及 neoecoae 的
 * {@code ECOCraftConfirmScreen}（走 ECO 规划器时 {@code ECOCraftConfirmScreenIntegration} 会
 * {@code setScreen} 把它顶替上来）。<b>注意后者是 {@code AEBaseScreen} 的直接子类，并不是
 * {@code CraftConfirmScreen} 的子类</b>，所以按 {@code CraftConfirmScreen} 判类型会漏掉它。</p>
 *
 * <p>两者的菜单都是 {@code CraftConfirmMenu}，而它<b>没有任何槽位</b>；EMI 的 {@code EmiScreenBase.of}
 * 对 {@code AbstractContainerScreen} 只在「菜单槽位非空」时才认作可用界面，于是这两个界面的
 * {@code EmiScreenBase} 恒为空 —— 侧栏、搜索框、以及「鼠标底下物品」判定全部失效。</p>
 *
 * <p>这里按「菜单是 {@code CraftConfirmMenu} 的 AE 界面」来匹配（同时覆盖上面两套界面）：
 * <ul>
 *     <li>通用 {@link dev.emi.emi.api.EmiScreenBoundsProvider}：提供窗口矩形，让侧栏与搜索框回来；</li>
 *     <li>通用 {@link dev.emi.emi.api.EmiStackProvider}：返回鼠标底下的计划条目，让 R/U/A 生效。</li>
 * </ul>
 * 用「通用」而不是按类注册，是因为 EMI 的 {@code EmiStackProviders}/{@code EmiScreenBase} 都是按
 * <b>精确类</b>查表的，按类注册对 {@code ECOCraftConfirmScreen} 这种子类/兄弟类不会命中。
 * 其它界面（菜单不是 {@code CraftConfirmMenu}）不受影响。</p>
 */
@EmiEntrypoint
public class GTAe2CraftConfirmEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        if (!ModList.get().isLoaded("ae2")) {
            return;
        }
        registry.addGenericScreenBoundsProvider(GTAe2CraftConfirmEmiPlugin::craftConfirmBounds);
        registry.addGenericStackProvider(GTAe2CraftConfirmEmiPlugin::craftConfirmStack);
    }

    private static boolean isCraftConfirm(AEBaseScreen<?> screen) {
        return screen.getMenu() instanceof CraftConfirmMenu;
    }

    private static Bounds craftConfirmBounds(Screen screen) {
        if (screen instanceof AEBaseScreen<?> ae && isCraftConfirm(ae)) {
            return new Bounds(ae.getGuiLeft(), ae.getGuiTop(), ae.getXSize(), ae.getYSize());
        }
        return null;
    }

    private static EmiStackInteraction craftConfirmStack(Screen screen, int mouseX, int mouseY) {
        if (!(screen instanceof AEBaseScreen<?> ae) || !isCraftConfirm(ae)) {
            return EmiStackInteraction.EMPTY;
        }
        StackWithBounds hovered = ae.getStackUnderMouse(mouseX, mouseY);
        if (hovered == null) {
            return EmiStackInteraction.EMPTY;
        }
        EmiStack stack = GTAe2EmiStacks.toEmiStack(hovered.stack().what());
        return stack == null || stack.isEmpty() ? EmiStackInteraction.EMPTY : new EmiStackInteraction(stack, null, false);
    }
}
