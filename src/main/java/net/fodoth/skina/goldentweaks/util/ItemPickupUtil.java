package net.fodoth.skina.goldentweaks.util;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.EventHooks;

public final class ItemPickupUtil {

    public static boolean canPickup(ItemEntity item) {

        return item.isAlive();
                //&& !item.hasPickUpDelay();
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
            pullToPlayer(player, item);
            return;
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