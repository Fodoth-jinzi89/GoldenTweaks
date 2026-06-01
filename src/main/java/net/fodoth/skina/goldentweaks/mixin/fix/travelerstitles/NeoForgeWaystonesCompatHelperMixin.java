package net.fodoth.skina.goldentweaks.mixin.fix.travelerstitles;

import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.yungnickyoung.minecraft.travelerstitles.services.NeoForgeWaystonesCompatHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NeoForgeWaystonesCompatHelper.class)
public class NeoForgeWaystonesCompatHelperMixin {

    @Inject(
            method = "updateClosestWaystone",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void goldentweaks$onlyTickLocalPlayer(PlayerTickEvent.Post event, CallbackInfo ci) {
        if (!(event.getEntity() instanceof LocalPlayer)) {
            ci.cancel();
        }
    }
}