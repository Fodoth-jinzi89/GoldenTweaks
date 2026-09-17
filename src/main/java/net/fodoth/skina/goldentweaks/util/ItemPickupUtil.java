package net.fodoth.skina.goldentweaks.util;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
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
    }

    /**
     * 拾取判定用的最大距离（创造/旁观者额外加成）。
     * <p>
     * 客户端射线（挑要发哪个实体）和服务端校验（防作弊复核）都要用，所以放在 common 工具里。
     * 它原先住在客户端专用的 {@code ClientClickHandler} 里，服务端包处理器一调用就会连带加载那个类，
     * 抛出 {@code NoClassDefFoundError: net/minecraft/client/player/LocalPlayer}，表现就是
     * “服务端里捡不起物品”。
     */
    public static double getMaxReach(Player player) {

        boolean extended =
                player.isCreative() || player.isSpectator();

        return GoldenTweaksCommonConfig.BASE_PICKUP_REACH.get()
                + (extended
                ? GoldenTweaksCommonConfig.EXTENDED_REACH_BONUS.get()
                : 0.0D);
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