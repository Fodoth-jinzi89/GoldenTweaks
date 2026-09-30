package net.fodoth.skina.goldentweaks.mixin.fix.silentgear;

import net.silentchaos512.gear.item.GearItemSet;
import net.silentchaos512.gear.setup.GearItemSets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Iterator;

// The gear creative tab is filled by iterating every gear set and building one
// stack per example material (16 x 35). Empty the iterator so the tab and EMI
// no longer show the pre-made tools; gear is crafted from parts, not taken from
// the creative menu.
@Mixin(value = GearItemSets.class, remap = false)
public class GearItemSetsMixin {

    @Inject(method = "getIterator", at = @At("HEAD"), cancellable = true)
    private static void gt$emptyGearCreativeTab(CallbackInfoReturnable<Iterator<GearItemSet<?>>> cir) {
        cir.setReturnValue(Collections.emptyIterator());
    }
}
