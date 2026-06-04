package net.fodoth.skina.goldentweaks.mixin.fix.aeronautics.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(
        targets = "dev.eriksonn.aeronautics.neoforge.events.AeroNeoForgeCommonEvents$ModBusEvents",
        remap = false
)
public interface AeroNeoForgeCommonEventsAccessor {

    @Invoker("jeiCompat")
    static void goldentweaks$invokeJeiCompat() {
        throw new AssertionError();
    }
}