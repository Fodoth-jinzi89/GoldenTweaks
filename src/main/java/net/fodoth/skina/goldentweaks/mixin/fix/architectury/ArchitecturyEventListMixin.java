package net.fodoth.skina.goldentweaks.mixin.fix.architectury;

import net.fodoth.skina.goldentweaks.util.SnapshotArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.function.Function;

@Mixin(targets = "dev.architectury.event.EventFactory$EventImpl")
public class ArchitecturyEventListMixin {

    @Shadow
    private ArrayList<?> listeners;

    @Inject(method = "<init>", at = @At("RETURN"), require = 0)
    private void gt$useSnapshotListeners(Function<?, ?> function, CallbackInfo ci) {
        this.listeners = new SnapshotArrayList<>();
    }
}
