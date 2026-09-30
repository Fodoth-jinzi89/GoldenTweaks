package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.search.EmiSearch;
import net.fodoth.skina.goldentweaks.compat.emi.GTBitSetHolder;
import net.fodoth.skina.goldentweaks.compat.emi.GTEmiSearchEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 把 EMI 搜索里最贵的那一步——「这个 stack 命中当前查询吗」——换成位集查询，见
 * {@link GTEmiSearchEngine#bitsFor}。
 *
 * <p>EMI 的 {@code SearchWorker#run} 会先 {@code new CompiledQuery(query)}，再用它逐项
 * {@code test} 扫一遍 source。所以位集挂在 {@code CompiledQuery} 自己身上最合适：构造时就绪、
 * 天然只属于这一次查询，不会和并发发起的另一次搜索串台，也不需要任何全局静态结果。</p>
 *
 * <p><b>刻意不改的地方：</b>不去取消 {@code EmiSearch#search}，也不自己写 {@code EmiSearch.stacks}。
 * 那样做会让 REMI 这类在 {@code SearchWorker#run} 里挂钩 {@code EmiSearch#apply} 的模组收不到结果
 * （现象就是"搜到了但侧栏不刷新"）。搜索流程保持 EMI 原样，我们只让逐项判定从
 * 「两三层 identity 集合查找 + 可能的正则」变成「一次下标查找 + 一次位测试」。</p>
 *
 * <p>不在索引里的 stack（例如别的模组临时构造、或非 INDEX 侧栏里的条目）返回 0 交回原实现，
 * 继续走 EMI 的 {@code matchesUnbaked}——那些实现与索引无关，因此行为不变。</p>
 */
@Mixin(value = EmiSearch.CompiledQuery.class, remap = false)
public abstract class EmiSearchCompiledQueryMixin implements GTBitSetHolder {

    /** 本查询的命中位集；为空表示本次交给 EMI 原生判定。 */
    @Unique
    private long[] gt$bits;

    @Override
    public long[] gt$bits() {
        return this.gt$bits;
    }

    /**
     * 必须用 {@code TAIL}：Mixin 不允许非 static 的 {@code @At("HEAD")} 注入落在构造器 {@code super()}
     * 之前，否则整个 mixin 应用失败、类加载直接抛错（而 {@code CompiledQuery} 是懒加载的，
     * 表现成"每次搜索都失败"）。位集只要在 {@code test} 之前就绪即可，构造结束时算完全够用。
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void gt$compileBitSet(String query, CallbackInfo ci) {
        this.gt$bits = GTEmiSearchEngine.bitsFor(query);
    }

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void gt$fastTest(EmiStack stack, CallbackInfoReturnable<Boolean> cir) {

        int answer = GTEmiSearchEngine.test(this.gt$bits, stack);

        if (answer != 0) {
            cir.setReturnValue(answer == 1);
        }
    }
}
