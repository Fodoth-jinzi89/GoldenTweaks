package net.fodoth.skina.goldentweaks.mixin.fix.krypton;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Krypton 0.1.0 的解压实现会把连接直接打断（客户端掉线报告 disconnect-2026-09-17_22.14.17-client.txt）：
 *
 * <pre>
 * io.netty.handler.codec.DecoderException: java.util.zip.DataFormatException: invalid code lengths set
 *   at java.util.zip.Inflater.inflate                                  &lt;- JDK 的 zlib
 *   at LAYER PLUGIN/velocity._native/com.velocitypowered.natives.compression.JavaVelocityCompressor.inflate
 *   at krypton/...MinecraftCompressDecoder.decode
 * -- Connection --  Protocol: configuration  Server brand: Youer   (服务端日志: Paper: Using Java compression from Velocity)
 * </pre>
 *
 * <p>Krypton 替换了原版的压缩解码器，并优先用自带（内嵌 jar-in-jar）的 Velocity 压缩库；native 没加载起来时
 * 退到它自带的 <b>纯 Java zlib</b>。这套实现在 Youer/Paper 服务端发来的压缩流上解不动
 * ⇒ 配置阶段就报 "Packet handling error" 掉线，整个连接再也起不来。</p>
 *
 * <p><b>修法</b>：保留 Krypton 的帧解析与全部校验，只把「解压」这一步换成<b>原版同款的 JDK
 * {@link Inflater}</b>（MC 自己的 {@code CompressionDecoder} 用的就是它，走本地 zlib）——
 * 也就是让客户端在这条链路上等价于没用 Krypton 的原版行为。正常解压后的缓冲、读指针、
 * 长度上限、threshold 校验都与 Krypton/原版保持一致；出入只在于解压实现本身。</p>
 *
 * <p>若连 JDK 的 Inflater 都解不动，说明压缩流真的坏了（那就不是 Krypton 的锅），
 * 这里会记一条 WARN 并照原样抛异常，诊断信息不丢。</p>
 */
@Mixin(targets = "me.steinborn.krypton.mod.shared.network.compression.MinecraftCompressDecoder", remap = false)
public abstract class MinecraftCompressDecoderMixin {

    @Shadow
    private int threshold;

    @Shadow
    @Final
    private boolean validate;

    @Shadow
    @Final
    private static int UNCOMPRESSED_CAP;

    @Unique
    private static final AtomicBoolean GT_REPORTED = new AtomicBoolean();

    @Inject(
            method = "decode(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void gt$inflateWithVanillaJdkInflater(ChannelHandlerContext ctx, ByteBuf in, List<Object> out,
                                                 CallbackInfo ci) {

        if (in.readableBytes() == 0) {
            ci.cancel();
            return;
        }

        FriendlyByteBuf buf = new FriendlyByteBuf(in);
        int length = buf.readVarInt();

        if (length == 0) {
            out.add(in.retain());
            ci.cancel();
            return;
        }

        if (validate) {
            if (length < threshold) {
                throw new DecoderException("Badly compressed packet - size of " + length
                        + " is below server threshold of " + threshold);
            }
            if (length > UNCOMPRESSED_CAP) {
                throw new DecoderException("Badly compressed packet - size of " + length
                        + " is larger than maximum of " + UNCOMPRESSED_CAP);
            }
        }

        byte[] compressed = new byte[buf.readableBytes()];
        buf.readBytes(compressed);

        byte[] result = new byte[length];
        Inflater inflater = new Inflater();
        int total;
        try {
            inflater.setInput(compressed);
            total = 0;
            while (total < length) {
                int inflated = inflater.inflate(result, total, length - total);
                if (inflated == 0) {
                    break;
                }
                total += inflated;
            }
        } catch (DataFormatException e) {
            if (GT_REPORTED.compareAndSet(false, true)) {
                GoldenTweaks.LOGGER.warn(
                        "[Krypton 兼容] 连原版 JDK Inflater 都解不动这个压缩帧（{}）—— 压缩流本身已经损坏，"
                                + "不是 Krypton 解压实现的锅；这条只打印一次",
                        e.toString()
                );
            }
            throw new DecoderException("Badly compressed packet - error " + e.getMessage(), e);
        } finally {
            inflater.end();
        }

        out.add(Unpooled.wrappedBuffer(result, 0, total));
        ci.cancel();
    }
}
