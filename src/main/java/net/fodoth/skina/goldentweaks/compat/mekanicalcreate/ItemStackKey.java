package net.fodoth.skina.goldentweaks.compat.mekanicalcreate;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * {@link ItemStack} 没有重写 {@code equals/hashCode}，不能直接放进 {@link java.util.HashSet}/{@link java.util.HashMap}。
 * 这个 key 与 {@code ItemStack.isSameItemSameComponents} 等价（物品 + 组件，忽略数量）。
 */
public record ItemStackKey(Item item, DataComponentMap components) {

    public static ItemStackKey of(ItemStack stack) {
        return new ItemStackKey(stack.getItem(), stack.getComponents());
    }
}
