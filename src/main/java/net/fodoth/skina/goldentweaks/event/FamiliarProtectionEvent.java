package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.util.FamiliarCompat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class FamiliarProtectionEvent {

    private FamiliarProtectionEvent() {
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {

        LivingEntity mob = event.getEntity();
        LivingEntity target = event.getNewAboutToBeSetTarget();

        if (target == null) {
            return;
        }

        if (FamiliarCompat.areFriendly(mob, target)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    /**
     * 阻止友军伤害
     */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {

        Entity attacker = event.getSource().getEntity();
        LivingEntity victim = event.getEntity();

        if (attacker == null) {
            return;
        }

        if (FamiliarCompat.areFriendly(attacker, victim)) {
            event.setCanceled(true);
        }
    }
}
