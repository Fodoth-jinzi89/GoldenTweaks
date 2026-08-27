package net.fodoth.skina.goldentweaks.mixin.fix.ae2;

import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.AEBaseMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MEStorageMenu.class)
public abstract class MEStorageMenuBroadcastMixin {
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
