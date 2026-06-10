package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksFamiliarCurioHelper;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksInvertedAttributesHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

public final class InvertedFamiliarSpellbookEvent {

    private InvertedFamiliarSpellbookEvent() {
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {

        LivingEntity entity =
                event.getEntity();

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        if (GoldenTweaksFamiliarCurioHelper.isInvertedSpellbook(
                event.getTo()
        )) {
            GoldenTweaksInvertedAttributesHelper.handleSpellbookChange(
                    player,
                    true
            );
        }

        if (GoldenTweaksFamiliarCurioHelper.isInvertedSpellbook(
                event.getFrom()
        )) {
            GoldenTweaksInvertedAttributesHelper.handleSpellbookChange(
                    player,
                    false
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        GoldenTweaksInvertedAttributesHelper.handlePlayerTick(player);
    }
}
