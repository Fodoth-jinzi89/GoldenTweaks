package net.fodoth.skina.goldentweaks.mixin.fix.ae2peat.accessor;

import appeng.menu.slot.RestrictedInputSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@Mixin(PatternEncodingAccessTermMenu.class)
public interface PatternEncodingAccessTermMenuAccessor {
    @Accessor("encodedPatternSlot")
    RestrictedInputSlot goldentweaks$getEncodedPatternSlot();
}
