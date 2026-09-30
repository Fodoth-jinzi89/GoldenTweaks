package net.fodoth.skina.goldentweaks.compat.academy;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.gear.util.GearHelper;

final class GearMagicAttack {

    static float highest(Player player) {
        return Math.max(magic(player.getMainHandItem()), magic(player.getOffhandItem()));
    }

    private static float magic(ItemStack stack) {
        if (!GearHelper.isGear(stack)) {
            return 0.0F;
        }

        float value = GearHelper.getMagicDamageModifier(stack);
        return value > 0.0F && Float.isFinite(value) ? value : 0.0F;
    }

    private GearMagicAttack() {
    }
}
