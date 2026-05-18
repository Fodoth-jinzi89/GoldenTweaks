package net.fodoth.skina.goldentweaks.util;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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
        ItemStack original = stack.copy();

        player.getInventory().add(stack);

        int inserted =
                original.getCount() - stack.getCount();

        if (inserted <= 0) {

            pullToPlayer(player, item);

            return;
        }

        player.awardStat(
                Stats.ITEM_PICKED_UP.get(original.getItem()),
                inserted
        );

        player.take(item, inserted);

        if (stack.isEmpty()) {
            item.discard();
            return;
        }

        if (!player.getInventory().add(stack.copy())) {
            pullToPlayer(player, item);
        }

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