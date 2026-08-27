package net.fodoth.skina.goldentweaks.mixin.fix.ae2peat;

import appeng.menu.AEBaseMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@Mixin(PatternEncodingAccessTermMenu.class)
public abstract class PatternEncodingAccessTermMenuBroadcastMixin {
    @Unique
    private long goldentweaks$lastBroadcastTick = Long.MIN_VALUE;

    @Inject(method = "broadcastChanges", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$skipDuplicateBroadcast(CallbackInfo ci) {
        var player = ((AEBaseMenu) (Object) this).getPlayer();
        if (player == null || player.level().getGameTime() == goldentweaks$lastBroadcastTick) {
            ci.cancel();
            return;
        }
        goldentweaks$lastBroadcastTick = player.level().getGameTime();
    }
}
