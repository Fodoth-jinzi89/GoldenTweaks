package net.fodoth.skina.goldentweaks.mixin.fix.gravitation;

import dev.simulated_team.simulated.network.packets.physics_staff.PhysicsStaffActionPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lets gravitation's physics staff act as simulated's own.
 *
 * <p>simulated validates that the sender is holding its staff before acting on the packet, and
 * refuses with {@code Invalid packet} otherwise. gravitation's staff drives the same physics but is a
 * different item, so without this the staff is inert.</p>
 *
 * <p>Two hooks, because the check is made against both hands: the main hand and then the offhand. Each
 * `is` call is redirected in place, so the two stay independent and either hand can satisfy the
 * check.</p>
 */
@Mixin(value = PhysicsStaffActionPacket.class, remap = false)
public abstract class PhysicsStaffActionPacketMixin {

    private static final String GRAVITATION_STAFF = "gravitation:physics_staff";

    @Redirect(method = "validateWorthyness",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/core/Holder;)Z",
                    ordinal = 0))
    private static boolean gt$acceptGravitationStaffMainHand(ItemStack stack, Holder<Item> originalStaff) {
        return gt$accepts(stack, originalStaff);
    }

    @Redirect(method = "validateWorthyness",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/core/Holder;)Z",
                    ordinal = 1))
    private static boolean gt$acceptGravitationStaffOffHand(ItemStack stack, Holder<Item> originalStaff) {
        return gt$accepts(stack, originalStaff);
    }

    /**
     * Whether a held stack counts as the staff.
     *
     * <p>Both conditions are checked: simulated's own item, or gravitation's by id. The id is compared
     * rather than resolved to an item holder so this stays correct when gravitation is absent — the
     * string simply never matches.</p>
     */
    private static boolean gt$accepts(ItemStack stack, Holder<Item> originalStaff) {
        return stack.is(originalStaff)
                || BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(GRAVITATION_STAFF);
    }
}
