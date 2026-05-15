package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = GoldenTweaks.MODID)
public class Click2PickEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickItem(PlayerInteractEvent.EntityInteractSpecific event) {

        if (event.isCanceled()) return;

        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Level level = event.getLevel();
        if (level.isClientSide) return;

        Entity target = event.getTarget();
        if (!(target instanceof ItemEntity itemEntity)) return;

        if (itemEntity.hasPickUpDelay()) return;

        if (!itemEntity.isAlive()) return;


        Player player = event.getEntity();

        ItemStack stack = itemEntity.getItem();
        ItemStack copy = stack.copy();

        if (player.getInventory().add(stack)) {

            player.awardStat(Stats.ITEM_PICKED_UP.get(copy.getItem()), copy.getCount());
            player.take(itemEntity, copy.getCount());

            if (stack.isEmpty()) {
                itemEntity.discard();
            }

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
