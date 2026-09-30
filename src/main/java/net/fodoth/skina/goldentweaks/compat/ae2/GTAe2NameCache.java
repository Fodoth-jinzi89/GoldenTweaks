package net.fodoth.skina.goldentweaks.compat.ae2;

import appeng.api.stacks.AEKey;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * AE2 ME 终端搜索用到的「已小写显示名」缓存。
 *
 * <p>{@code NameSearchPredicate#test} 每调用一次就做一遍
 * {@code entry.getWhat().getDisplayName().getString().toLowerCase()}：
 * 为每个条目重新构造一个可翻译 {@code Component}、解析翻译、拼字符串、再小写。
 * 这对同一个 {@code AEKey} 结果恒定，缓存成一个 {@code literal} 即可——
 * 之后 {@code getString()} 只是取现成字符串，翻译解析那一步整块省掉。</p>
 *
 * <p><b>为什么只缓存到「小写名 Component」这一步、而不接管整个匹配：</b>
 * JustEnoughCharacters 给 AE 的拼音，是把它自己的 {@code String.contains} 换进 AE 的谓词类里的
 * （见其 {@code targets.json} 的 contains 段，其中就有 {@code NameSearchPredicate:test}）。
 * 一旦我们把整个 {@code test} 换掉，那次 {@code contains} 调用就不在 JEChars 的改写范围内了，
 * <b>拼音会静默失效</b>——所以这里只 redirect {@code getDisplayName()}，
 * {@code contains(term)} 原样留在原方法体里。</p>
 *
 * <p>做法对齐 AE2 自己的 {@code RepoSearch#tooltipCache}（{@code WeakHashMap<AEKey,String>}）：
 * 弱键，条目随网络内容消失后自然回收。</p>
 */
public final class GTAe2NameCache {

    private static final Map<AEKey, Component> NAMES = new WeakHashMap<>();

    private GTAe2NameCache() {
    }

    /** 该 key 的显示名，已小写；调用方随后可安全地再调一次 {@code toLowerCase}。 */
    public static Component loweredDisplayName(AEKey key) {

        Component cached = NAMES.get(key);
        if (cached == null) {
            cached = Component.literal(key.getDisplayName().getString().toLowerCase());
            NAMES.put(key, cached);
        }
        return cached;
    }
}
