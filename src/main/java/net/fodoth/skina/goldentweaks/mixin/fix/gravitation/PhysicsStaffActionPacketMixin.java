package net.fodoth.skina.goldentweaks.mixin.fix.gravitation;

import dev.simulated_team.simulated.network.packets.physics_staff.PhysicsStaffActionPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PhysicsStaffActionPacket.class, remap = false)
public abstract class PhysicsStaffActionPacketMixin {
    @Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/core/Holder;)Z"))
    private boolean gt$acceptGravitationStaff(ItemStack stack, Holder<Item> originalStaff) {
        return stack.is(originalStaff)
                || BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("gravitation:physics_staff");
    }
}
