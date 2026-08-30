package net.fodoth.skina.goldentweaks.mixin.fix.extendedae_plus.accessor;

import appeng.client.Point;
import appeng.client.gui.widgets.VerticalButtonBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VerticalButtonBar.class)
public interface VerticalButtonBarAccessor {

    @Accessor("position")
    Point gt$getPosition();
}
