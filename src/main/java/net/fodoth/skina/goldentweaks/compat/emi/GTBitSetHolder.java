package net.fodoth.skina.goldentweaks.compat.emi;

/**
 * 由 {@code EmiSearchCompiledQueryMixin} 让 {@code EmiSearch.CompiledQuery} 实现，
 * 好让搜索内核能取回那次查询自己算好的命中位集。
 *
 * <p>位集在 {@code CompiledQuery} 构造时就地算一次（见 {@link GTEmiSearchEngine#bitsFor}），
 * 内核直接复用它去物化结果列表，避免「算两遍」。</p>
 */
public interface GTBitSetHolder {

    /** 命中位集（烘焙栈下标空间）；为 null 表示本次没有可用的位集。 */
    long[] gt$bits();
}
