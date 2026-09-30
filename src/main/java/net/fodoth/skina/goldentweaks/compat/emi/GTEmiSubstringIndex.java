package net.fodoth.skina.goldentweaks.compat.emi;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * 1~3 gram 倒排索引：把「子串搜索」从「遍历全部字符串」降到「只触碰命中候选」。
 *
 * <p>建索引时把每个 key 的 1/2/3-gram 全部写入 {@code gram -> 条目下标数组}，posting 天然严格递增；
 * 查询时：</p>
 * <ul>
 *   <li>token 长度 ≤ 3：一次 gram 命中即结果，<b>零误报</b>，无需校验；</li>
 *   <li>token 长度 &gt; 3：取各 3-gram 的 posting，<b>按 posting 长度升序</b>（最稀有的先算）两两有序求交，
 *       再对候选做一次 {@link String#contains} 校验——误报只可能来自 gram 共现，不会漏报。</li>
 * </ul>
 *
 * <p>这条路线与 JEI 19+ 自研的 {@code BakedSubstringIndex} 同构，是 EMI 原版
 * {@link net.minecraft.client.searchtree.SuffixArray} 的替代：原版 {@code generate()} 是比较器驱动的
 * 倍增排序，代价约 {@code O(N log N × log min(N,maxLen))}（N = 字符总数），在大型整合包里是秒级开销；
 * 本结构建索引只与字符总数成正比，且没有排序。</p>
 *
 * <p>索引是只读的，构建完成后可被搜索线程安全共享。</p>
 */
public final class GTEmiSubstringIndex {

    private static final int[] NO_ENTRIES = new int[0];

    private final String[] keys;
    private final int[] stacks;
    private final Long2ObjectOpenHashMap<int[]> grams;

    private GTEmiSubstringIndex(String[] keys, int[] stacks, Long2ObjectOpenHashMap<int[]> grams) {
        this.keys = keys;
        this.stacks = stacks;
        this.grams = grams;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int keyCount() {
        return this.keys.length;
    }

    public int gramCount() {
        return this.grams.size();
    }

    /** 把 token 命中的所有 stack 下标交给 sink（同一 stack 可能被多次回调，调用方需幂等）。 */
    public void forEachMatch(String token, IntConsumer sink) {

        if (token.isEmpty()) {
            return;
        }

        int length = token.length();

        if (length <= 3) {
            int[] entries = this.grams.get(encodeGram(token, 0, length));
            if (entries == null) {
                return;
            }
            for (int entry : entries) {
                sink.accept(this.stacks[entry]);
            }
            return;
        }

        for (int entry : this.candidates(token)) {
            if (this.keys[entry].contains(token)) {
                sink.accept(this.stacks[entry]);
            }
        }
    }

    private int[] candidates(String token) {

        int length = token.length();
        int maxGrams = length - 2;

        long[] gramKeys = new long[maxGrams];
        int[][] postings = new int[maxGrams][];
        int count = 0;

        for (int offset = 0; offset <= length - 3; offset++) {

            long gram = encodeGram(token, offset, 3);

            boolean duplicate = false;
            for (int i = 0; i < count; i++) {
                if (gramKeys[i] == gram) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                continue;
            }

            int[] entries = this.grams.get(gram);
            if (entries == null) {
                return NO_ENTRIES;
            }

            int at = count;
            while (at > 0 && postings[at - 1].length > entries.length) {
                gramKeys[at] = gramKeys[at - 1];
                postings[at] = postings[at - 1];
                at--;
            }
            gramKeys[at] = gram;
            postings[at] = entries;
            count++;
        }

        if (count == 0) {
            return NO_ENTRIES;
        }

        int[] current = postings[0];
        for (int i = 1; i < count; i++) {
            current = intersect(current, postings[i]);
            if (current.length == 0) {
                return NO_ENTRIES;
            }
        }
        return current;
    }

    private static int[] intersect(int[] left, int[] right) {

        int[] output = new int[Math.min(left.length, right.length)];
        int i = 0;
        int j = 0;
        int n = 0;

        while (i < left.length && j < right.length) {
            int a = left[i];
            int b = right[j];
            if (a == b) {
                output[n++] = a;
                i++;
                j++;
            } else if (a < b) {
                i++;
            } else {
                j++;
            }
        }

        return n == output.length ? output : Arrays.copyOf(output, n);
    }

    private static long encodeGram(String string, int offset, int length) {

        long gram = (long) length << 48;
        for (int i = 0; i < length; i++) {
            gram |= (long) string.charAt(offset + i) << ((2 - i) * 16);
        }
        return gram;
    }

    public static final class Builder {

        private final List<String> keys = new ArrayList<>();
        private final List<Integer> stacks = new ArrayList<>();

        private Builder() {
        }

        public void put(String key, int stack) {
            this.keys.add(key);
            this.stacks.add(stack);
        }

        public GTEmiSubstringIndex build() {

            int count = this.keys.size();
            String[] keyArray = this.keys.toArray(new String[0]);
            int[] stackArray = new int[count];
            for (int i = 0; i < count; i++) {
                stackArray[i] = this.stacks.get(i);
            }

            Long2ObjectOpenHashMap<IntArrayList> mutable = new Long2ObjectOpenHashMap<>();
            Long2IntOpenHashMap lastEntryByGram = new Long2IntOpenHashMap();
            lastEntryByGram.defaultReturnValue(-1);

            for (int entry = 0; entry < count; entry++) {
                addGrams(mutable, lastEntryByGram, keyArray[entry], entry);
            }

            Long2ObjectOpenHashMap<int[]> grams = new Long2ObjectOpenHashMap<>(mutable.size());
            for (Long2ObjectMap.Entry<IntArrayList> e : mutable.long2ObjectEntrySet()) {
                grams.put(e.getLongKey(), e.getValue().toIntArray());
            }

            return new GTEmiSubstringIndex(keyArray, stackArray, grams);
        }

        private static void addGrams(Long2ObjectOpenHashMap<IntArrayList> grams, Long2IntOpenHashMap lastEntryByGram,
                                     String key, int entry) {

            int keyLength = key.length();

            for (int gramLength = 1; gramLength <= 3 && gramLength <= keyLength; gramLength++) {
                for (int offset = 0; offset <= keyLength - gramLength; offset++) {

                    long gram = encodeGram(key, offset, gramLength);

                    // 同一条目内同一个 gram 会出现多次（例如 "aaa" 的 1-gram 'a'），只记一次
                    if (lastEntryByGram.get(gram) == entry) {
                        continue;
                    }

                    IntArrayList entries = grams.get(gram);
                    if (entries == null) {
                        entries = new IntArrayList();
                        grams.put(gram, entries);
                    }
                    entries.add(entry);
                    lastEntryByGram.put(gram, entry);
                }
            }
        }
    }
}
