package net.fodoth.skina.goldentweaks.mixin.fix.thaumichorizons;

import com.kentington.thaumichorizons.common.vat.LegacyInfusionIngredients;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the vat's damageable ingredients match any copy of the item, not just an untouched one.
 *
 * <p>{@code LegacyInfusionIngredients.exact} maps a plain object to a strict
 * {@code DataComponentIngredient} ({@code of(true, stack)}) whenever the item is damageable. A
 * strict data-component ingredient only matches a stack whose <em>component patch is exactly the
 * default one</em>, so a shears that carries any extra component, an explicit damage value, or any
 * other difference ends up rejected even though it is the right item. The auto-shearing sheep and
 * the scholar chicken - the only recipes that go through this branch - are exactly the "shears"
 * recipes, and their pedestal ingredient would then never match, which makes a correctly built,
 * fully researched vat infusion silently refuse to start (no sound, no animation).</p>
 *
 * <p>Matching by item alone restores the original 1.7.10 behaviour, where the legacy matcher only
 * cared about the item and, for the recipes that opted in, a damage wildcard. Non-damageable items
 * already take the plain-item branch, so this only changes the damageable case.</p>
 */
@Mixin(value = LegacyInfusionIngredients.class, remap = false)
public class LegacyInfusionIngredientsMixin {

    @Inject(method = "exact", at = @At("HEAD"), cancellable = true)
    private static void gt$matchAnyCopyOfItem(Item item, CallbackInfoReturnable<Ingredient> cir) {
        cir.setReturnValue(Ingredient.of(item));
    }
}
