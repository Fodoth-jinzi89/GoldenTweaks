package net.fodoth.skina.goldentweaks.mixin.shut;

import net.mcreator.createstuffadditions.events.ClientEvents.ModBusEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mixin(ModBusEvents.class)
public class ModBusEventsMixin {

    @Inject(
            method = "addEntityRendererLayers",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void goldentweaks$disableArmorRenderers(
            EntityRenderersEvent.AddLayers event,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}