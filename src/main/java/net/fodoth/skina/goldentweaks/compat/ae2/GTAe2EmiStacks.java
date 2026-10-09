package net.fodoth.skina.goldentweaks.compat.ae2;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.stack.EmiStack;

/**
 * AE2 {@link AEKey} → EMI {@link EmiStack} 的转换：先走 AE2 自己注册的转换器，取不到再用
 * item / fluid 两条直连兜底，避免因为转换器注册时序问题把收藏功能一起拖死。
 */
public final class GTAe2EmiStacks {

    private GTAe2EmiStacks() {
    }

    public static EmiStack toEmiStack(AEKey key) {
        EmiStack converted = EmiStackHelper.toEmiStack(new GenericStack(key, 1L));
        if (converted != null && !converted.isEmpty()) {
            return converted;
        }
        if (key instanceof AEItemKey itemKey) {
            return EmiStack.of(itemKey.getReadOnlyStack());
        }
        if (key instanceof AEFluidKey fluidKey) {
            return NeoForgeEmiStack.of(fluidKey.toStack(1));
        }
        return null;
    }
}
