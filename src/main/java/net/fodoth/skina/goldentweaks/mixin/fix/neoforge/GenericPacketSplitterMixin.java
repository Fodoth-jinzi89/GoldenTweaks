package net.fodoth.skina.goldentweaks.mixin.fix.neoforge;

import io.netty.channel.ChannelHandlerContext;
import net.neoforged.neoforge.network.filters.GenericPacketSplitter;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.SplitPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericPacketSplitter.class)
public class GenericPacketSplitterMixin {

    @Inject(
            method = "handle",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    @SuppressWarnings("all")
    private static void goldentweaks$preventNullContext(
            SplitPacketPayload payload, IPayloadContext context, CallbackInfo ci
    ) {

        ChannelHandlerContext nettyContext =
                context.channelHandlerContext();

        if (nettyContext == null) {
            ci.cancel();
        }
    }
}