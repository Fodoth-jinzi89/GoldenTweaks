package net.fodoth.skina.goldentweaks.mixin.fix.citresewn;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

/** Runs after CIT Resewn's default-priority mixins; its postApply moves initializers. */
@Mixin(value = ItemStack.class, priority = 100)
public abstract class ItemStackMixin {
}
