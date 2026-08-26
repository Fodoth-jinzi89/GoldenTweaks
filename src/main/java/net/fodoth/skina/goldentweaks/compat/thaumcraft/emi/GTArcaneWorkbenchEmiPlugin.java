package net.fodoth.skina.goldentweaks.compat.thaumcraft.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.neoforged.fml.ModList;
import thaumcraft.common.registry.TCMenuTypes;

/**
 * GoldenTweaks 的 EMI 入口：为奥术工作台注册配方转移处理器，
 * 使其支持与普通工作台一致的 "+ 号按钮" 自动转移配方。
 */
@EmiEntrypoint
public class GTArcaneWorkbenchEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        if (!ModList.get().isLoaded("thaumcraft")) {
            return;
        }
        registry.addRecipeHandler(
                TCMenuTypes.ARCANE_WORKBENCH.get(),
                new ArcaneWorkbenchEmiRecipeHandler()
        );
    }
}
