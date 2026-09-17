package net.fodoth.skina.goldentweaks.mixin.fix.watut;

import com.corosus.watut.network.PacketNBTFromServer;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fodoth.skina.goldentweaks.compat.watut.WatutNbtTolerantCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * WATUT 1.2.7 的 {@code watut:nbt_client}（服务端→客户端的玩家状态 NBT）一旦解码失败就会打断整条连接：
 * 客户端 netty 抛 {@code DecoderException} → “Failed to decode packet
 * 'clientbound/minecraft:custom_payload'” → 玩家掉线。
 *
 * <p>实测链（latest.log 21:19:02）：{@code FriendlyByteBuf.readNbt} →
 * {@code UTFDataFormatException: malformed input: partial character at end}，
 * 也就是收到的 NBT 在中途被截断。两侧 mod 版本相同（1.2.7），且该载荷的写/读是对称的
 * （{@code writeNbt}/{@code readNbt}），所以这里不猜 NBT 内容，只把 **STREAM_CODEC 的解码**包一层，
 * 让坏载荷不至于踢人；正常解码/编码路径完全不变。</p>
 */
@Mixin(value = PacketNBTFromServer.class, remap = false)
public abstract class PacketNBTFromServerMixin {

    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;"
            ),
            remap = false
    )
    private static StreamCodec<RegistryFriendlyByteBuf, PacketNBTFromServer> gt$tolerantNbtCodec(
            StreamCodec<RegistryFriendlyByteBuf, PacketNBTFromServer> original
    ) {
        return new WatutNbtTolerantCodec(original);
    }
}
