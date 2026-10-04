package net.fodoth.skina.goldentweaks.util;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.EventHooks;

public final class ItemPickupUtil {

    public static boolean canPickup(ItemEntity item) {
        return item.isAlive();
    }

    /**
     * 玩家主背包（快捷栏 + 背包）的槽位数。
     * <p>
     * {@link Inventory#getContainerSize()} 返回的是「主背包 + 盔甲 + 副手」的总数（41），
     * 不能直接拿来判断背包是否放得下——否则空着的盔甲/副手槽会被误判成有空位。
     * 拾取只会进入主背包与快捷栏，因此这里只统计前 36 格。
     */
    private static final int MAIN_INVENTORY_SIZE = 36;

    /**
     * 玩家背包能否容纳整组物品：主背包有空槽，或存在可叠加且未满的同种物品。
     * <p>
     * 客户端（要不要发拾取请求）与服务端（要不要执行）都用于「背包已满时能否右键拾取」的判定，
     * 所以放在 common 工具里。只统计主背包与快捷栏，不含盔甲与副手。
     */
    public static boolean canFit(Player player, ItemStack stack) {

        if (stack.isEmpty()) {
            return true;
        }

        Inventory inventory = player.getInventory();

        // getFreeSlot 只扫描主背包（items），因此不会把盔甲/副手的空槽算进来
        if (inventory.getFreeSlot() != -1) {
            return true;
        }

        int max = stack.getMaxStackSize();
        int slots = Math.min(inventory.getContainerSize(), MAIN_INVENTORY_SIZE);

        for (int slot = 0; slot < slots; slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (existing.getCount() < Math.min(max, existing.getMaxStackSize())
                    && ItemStack.isSameItemSameComponents(existing, stack)) {
                return true;
            }
        }

        return false;
    }

    public static void pickup(Player player, ItemEntity item) {

        if (!canPickup(item)) {
            return;
        }

        ItemStack stack = item.getItem();
        ItemStack originalCopy = stack.copy();

        TriState result =
                EventHooks.fireItemPickupPre(item, player).canPickup();

        if (result.isFalse()) {
            return;
        }

        if (!player.getInventory().add(stack)) {
            // 创造模式：背包满也视为拾取成功（清空栈走下面的正常结算逻辑），
            // 避免物品被 pullToPlayer 拉到空中后因背包满而掉回地面。
            if (!player.getAbilities().instabuild) {
                pullToPlayer(player, item);
                return;
            }
            stack.setCount(0);
        }

        EventHooks.fireItemPickupPost(
                item,
                player,
                originalCopy
        );

        int inserted =
                originalCopy.getCount() - stack.getCount();

        player.take(item, inserted);

        if (stack.isEmpty()) {
            item.discard();
            stack.setCount(inserted);
        }

        player.awardStat(
                Stats.ITEM_PICKED_UP.get(originalCopy.getItem()),
                inserted
        );

        player.onItemPickup(item);
    }

    public static void pullToPlayer(Player player, ItemEntity item) {

        item.setExtendedLifetime();

        Vec3 targetPos =
                player.position().add(0.0D, 0.3D, 0.0D);

        Vec3 motion = targetPos.subtract(item.position())
                .normalize()
                .scale(0.35D);

        item.setDeltaMovement(motion);

        item.hasImpulse = true;
    }

    private ItemPickupUtil() {
    }
}