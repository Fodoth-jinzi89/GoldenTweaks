package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.search.TooltipQuery;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * EMI 的 {@code TooltipQuery#getText} 没给 {@code getTooltipText()} 判空：
 *
 * <pre>
 * List&lt;Component&gt; lines = stack.getTooltipText();
 * return lines.isEmpty() ? lines : lines.subList(1, lines.size());   // ← lines 可能是 null
 * </pre>
 *
 * <p>而某些模组的 {@code EmiStack} 实现确实会返回 {@code null}（EMI 自己的
 * {@code EmiSearch#bake} 就判了 null，所以它一直没暴露）。这条路径在我们对「不在索引里的 stack」
 * 回落 {@code matchesUnbaked} 时会被走到，于是整个查询直接 NPE 失败 —— 现象就是搜索框里
 * 某些词（如 {@code zuanshi}）完全没反应。</p>
 *
 * <p>这里把 null 统一成空表，语义与「没有提示行」一致。</p>
 */
@Mixin(value = TooltipQuery.class, remap = false)
public abstract class TooltipQueryMixin {

    @Redirect(method = "getText",
            at = @At(value = "INVOKE",
                    target = "Ldev/emi/emi/api/stack/EmiStack;getTooltipText()Ljava/util/List;"))
    private static List<Component> goldentweaks$nonNullTooltip(EmiStack stack) {
        List<Component> lines = stack.getTooltipText();
        return lines == null ? List.of() : lines;
    }
}
