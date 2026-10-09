package net.fodoth.skina.goldentweaks.compat.jei;

import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.world.item.ItemStack;

/**
 * 让「遍历全部三万多物品」的 JEI 插件扫描变便宜。
 *
 * <p>神秘时代的 {@code ThaumcraftJeiPlugin#hasSafeJeiUid} 与 scex_aspect_jei 的
 * {@code AspectCatalog#add} 都对每个物品调用 {@code IStackHelper#getUidForStack}
 * （在 TMRV 内置的 JEI 里会走 subtype interpreter 链，实测约 350µs/次），各自两次，
 * 且都发生在去重之前 —— 一次重载里合计约 22s，两个插件并行也就是那 24.7s 墙钟。</p>
 *
 * <p>它们遍历的都是 {@code item.getDefaultInstance()}，即<b>不带自定义 components</b>。
 * 此时同一个 {@link net.minecraft.world.item.Item} 的两个默认 stack 在结构上完全相同，
 * 任何确定性 UID 函数都必然给它们相同的值，所以去重键直接用 {@code Item} 即可，无需调用 JEI。
 * 实测本整合包里 {@code rejected=0} —— 那个「探错」调用一次都没真正救过场。</p>
 *
 * <p>带自定义 components 的物品一律走原逻辑，保证结果一致。</p>
 */
public final class GTJeiAspectScan {

    private GTJeiAspectScan() {
    }

    /** 是否可以对这种 stack 走快路径。 */
    public static boolean fastScan(ItemStack stack) {
        return GoldenTweaksClientConfig.JEI_ASPECT_SCAN_SKIP_UID.get() && stack.getComponentsPatch().isEmpty();
    }

    /** 去重键：快路径下直接用 Item，否则老实调 JEI。 */
    public static Object dedupKey(IStackHelper helper, ItemStack stack, UidContext context) {
        return fastScan(stack) ? stack.getItem() : helper.getUidForStack(stack, context);
    }

    /** 那个返回值被丢掉的「探错」调用：快路径下直接跳过。 */
    public static Object probeOrNull(IStackHelper helper, ItemStack stack, UidContext context) {
        return fastScan(stack) ? null : helper.getUidForStack(stack, context);
    }
}
