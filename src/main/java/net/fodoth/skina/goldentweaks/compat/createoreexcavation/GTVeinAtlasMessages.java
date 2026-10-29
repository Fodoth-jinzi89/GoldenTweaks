package net.fodoth.skina.goldentweaks.compat.createoreexcavation;

/**
 * 批量把矿脉写进图鉴时，抑制 {@code OreVeinAtlasItem#addVein} 每次调用都会发的
 * 「矿脉已添加至矿脉图鉴」提示（见 mixin/balance/createoreexcavation/OreVeinAtlasItemSilentAddMixin）。
 *
 * <p>只在服务端主线程上由黄金探矿杖成批写入时置位，写入结束立刻复位。</p>
 */
public final class GTVeinAtlasMessages {

    private static boolean batch;

    private GTVeinAtlasMessages() {
    }

    public static boolean isBatch() {
        return batch;
    }

    static void beginBatch() {
        batch = true;
    }

    static void endBatch() {
        batch = false;
    }
}
