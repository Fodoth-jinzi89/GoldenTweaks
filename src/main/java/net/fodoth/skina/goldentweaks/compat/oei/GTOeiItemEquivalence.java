package net.fodoth.skina.goldentweaks.compat.oei;

import com.mafuyu404.oneenoughitem.client.ClientContext;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * OEI 会在构造 {@link ItemStack} 时把来源物品替换成统一目标物品。SI 的配方结果对象往往在
 * OEI 替换关闭的上下文中构造（或早于替换表建立），于是机器里保存的产出是统一后的物品，
 * 而每次重新取出的配方结果仍是来源物品；原版 {@code ItemStack.isSameItem(ItemStack, ItemStack)}
 * 与 {@code isSameItemSameComponents} 会判成不同物品，机器就认为输出槽里是「别的产物」而停摆。
 *
 * <p>这里按 OEI 的映射把两个物品都收敛到统一目标后再比较；组件只比较显式 patch，避免把两个
 * 带不同自定义数据的同目标物品合并。</p>
 *
 * <p>哈希必须与上面的等价判断保持一致：原版 {@code ItemStack.hashItemAndComponents} 仍按真实物品
 * 计算，而这里的等价判断会把来源物品与统一目标视为相同。两者不一致会破坏
 * {@code ItemStackLinkedSet}（创造模式标签使用它）的哈希/等价契约，在标签里同时出现同一矿物的
 * 多个变体（如锇矿石与深层锇矿石）时抛出
 * {@code Accidentally adding the same item stack twice} 崩溃。</p>
 *
 * <p>创造模式标签构建期间必须整体绕过：OEI 在 {@code CreativeModeTab.buildContents} 里刻意跳过
 * 物品替换（{@code ClientContext.isBuilding()}），让标签按原样收录物品；此时若仍把不同变体判定为
 * 同一物品，NeoForge 的 {@code BuildCreativeModeTabContentsEvent} 与标签自身的去重集合都会把
 * 它们当成重复项并抛异常。调用方需先用 {@link #isCreativeTabBuilding()} 判断。</p>
 */
public final class GTOeiItemEquivalence {

    private static final int MAX_REPLACEMENT_DEPTH = 16;

    private GTOeiItemEquivalence() {
    }

    /** 创造模式标签构建中：此时必须完全沿用原版比较，不能把统一映射带进去。 */
    public static boolean isCreativeTabBuilding() {
        return ClientContext.isBuilding();
    }

    public static boolean isSameItem(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return false;
        }
        return canonicalItemId(first.getItem()).equals(canonicalItemId(second.getItem()));
    }

    public static boolean isSameItemSameComponents(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return false;
        }
        if (!first.getComponentsPatch().equals(second.getComponentsPatch())) {
            return false;
        }
        return canonicalItemId(first.getItem()).equals(canonicalItemId(second.getItem()));
    }

    /**
     * 与 {@link #isSameItemSameComponents} 配套的哈希：同样收敛到统一目标，且只使用显式 patch，
     * 保证「相等则同哈希」。
     */
    public static int hashItemAndComponents(ItemStack stack) {
        if (stack == null) {
            return 0;
        }
        int hash = 31 + canonicalItemId(stack.getItem()).hashCode();
        return 31 * hash + stack.getComponentsPatch().hashCode();
    }

    private static String canonicalItemId(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        for (int depth = 0; depth < MAX_REPLACEMENT_DEPTH; depth++) {
            String target = ItemReplacementCache.matchItem(id);
            if (target == null || target.equals(id)) {
                return id;
            }
            id = target;
        }
        return id;
    }
}
