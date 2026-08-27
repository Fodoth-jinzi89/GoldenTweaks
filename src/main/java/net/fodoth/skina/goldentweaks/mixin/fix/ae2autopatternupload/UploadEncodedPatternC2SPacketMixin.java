package net.fodoth.skina.goldentweaks.mixin.fix.ae2autopatternupload;

import com.gali.ae2_auto_pattern_upload.network.UploadEncodedPatternC2SPacket;
import net.fodoth.skina.goldentweaks.compat.ae2autopatternupload.PeatPatternUploadCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@Mixin(UploadEncodedPatternC2SPacket.class)
public abstract class UploadEncodedPatternC2SPacketMixin {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gt$handlePeat(UploadEncodedPatternC2SPacket packet, IPayloadContext context, CallbackInfo ci) {
        if (context.player().containerMenu instanceof PatternEncodingAccessTermMenu menu) {
            context.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) context.player();
                boolean success = PeatPatternUploadCompat.upload(menu, (int) (-1L - packet.providerId()));
                if (packet.showStatusMessage()) {
                    player.sendSystemMessage(Component.translatable(success
                                    ? "ae2_auto_pattern_upload.screen.upload.auto_success"
                                    : "ae2_auto_pattern_upload.screen.upload.auto_failed",
                            packet.providerName().isBlank() ? "#" + packet.providerId() : packet.providerName()));
                }
            });
            ci.cancel();
        }
    }
}
