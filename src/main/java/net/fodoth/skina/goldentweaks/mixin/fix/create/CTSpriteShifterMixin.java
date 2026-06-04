package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(CTSpriteShifter.class)
public class CTSpriteShifterMixin {

    @Shadow(remap = false)
    @Final
    @Mutable
    private static Map<String, CTSpriteShiftEntry> ENTRY_CACHE;

    @Inject(
            method = "<clinit>",
            at = @At("TAIL"),
            remap = false
    )
    private static void goldenTweaks$replaceCache(CallbackInfo ci) {
        ENTRY_CACHE = new ConcurrentHashMap<>(ENTRY_CACHE);
    }
}