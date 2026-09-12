package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.jemi.impl.extras.JemiScrollGridWidget;
import net.fodoth.skina.goldentweaks.util.emi.ScexAspectJeiScrollAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = JemiScrollGridWidget.class, remap = false)
public abstract class ScexAspectJeiScrollStateMixin implements ScexAspectJeiScrollAccess {
    @Unique
    private int goldentweaks$offset;

    @Override
    public int goldentweaks$getOffset() {
        return goldentweaks$offset;
    }

    @Override
    public void goldentweaks$setOffset(int offset) {
        goldentweaks$offset = offset;
    }
}
