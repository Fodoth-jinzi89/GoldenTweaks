package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.GoldenDandelion;
import net.fodoth.skina.goldentweaks.util.GTFrozenAge;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class GoldenDandelionEvent {
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getItemStack().is(GoldenDandelion.ITEM.get())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        if (event.getLevel().isClientSide()) return;
        var player = event.getEntity();
        if (!(event.getTarget() instanceof AgeableMob mob) || !mob.isBaby()) {
            player.sendSystemMessage(Component.translatable("message.goldentweaks.golden_dandelion.not_baby")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        var frozenAge = (GTFrozenAge) mob;
        boolean frozen = !frozenAge.gt$isAgeFrozen();
        frozenAge.gt$setAgeFrozen(frozen);
        if (!frozen) mob.setAge(AgeableMob.BABY_START_AGE);
        mob.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable("message.goldentweaks.golden_dandelion."
                + (frozen ? "frozen" : "unfrozen")).withStyle(frozen ? ChatFormatting.YELLOW : ChatFormatting.GREEN));
        if (!player.getAbilities().instabuild) event.getItemStack().shrink(1);
    }
}
