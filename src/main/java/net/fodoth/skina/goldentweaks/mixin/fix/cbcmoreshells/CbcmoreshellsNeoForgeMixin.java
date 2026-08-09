package net.fodoth.skina.goldentweaks.mixin.fix.cbcmoreshells;

import com.cainiao1053.cbcmoreshells.CbcmoreshellsNeoForge;
import com.cainiao1053.cbcmoreshells.index.CBCMSSoundEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CbcmoreshellsNeoForge.class)
public class CbcmoreshellsNeoForgeMixin {

    @Inject(
            method = "onRegisterSounds",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fixDuplicateSounds(
            RegisterEvent event, CallbackInfo ci
    ) {

        if (!event.getRegistryKey().equals(Registries.SOUND_EVENT))
            return;


        event.register(
                Registries.SOUND_EVENT,
                helper -> CBCMSSoundEvents.ALL.values()
                        .stream()
                        .filter(entry ->
                                !BuiltInRegistries.SOUND_EVENT.containsKey(entry.getId())
                        )
                        .forEach(entry ->
                                entry.register(helper)
                        )
        );

        ci.cancel();
    }
}
