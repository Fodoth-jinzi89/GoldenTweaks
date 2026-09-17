package net.fodoth.skina.goldentweaks.compat.watut;

import com.corosus.watut.network.PacketNBTFromServer;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code watut:nbt_client} 载荷用的容错 codec：解码失败时丢弃该载荷，而不是让连接崩掉。
 *
 * <p>NeoForge/原版的载荷解码只要抛异常就会关掉整条连接，客户端表现就是
 * {@code Failed to decode packet 'clientbound/minecraft:custom_payload'} + 玩家掉线。</p>
 *
 * <p>编码路径与正常解码路径完全交给 {@code delegate}，行为不变；只有解码失败这一条路被改成
 * 「记一次日志 + 返回空 NBT」。空 NBT 进到 WATUT 的 {@code handle} 后，它那几个
 * {@code contains(...)} 判断全为 false ⇒ 这次状态同步被跳过，其余一切照常。</p>
 */
public final class WatutNbtTolerantCodec implements StreamCodec<RegistryFriendlyByteBuf, PacketNBTFromServer> {

    private static final AtomicBoolean REPORTED = new AtomicBoolean();

    private final StreamCodec<RegistryFriendlyByteBuf, PacketNBTFromServer> delegate;

    public WatutNbtTolerantCodec(StreamCodec<RegistryFriendlyByteBuf, PacketNBTFromServer> delegate) {
        this.delegate = delegate;
    }

    @Override
    public PacketNBTFromServer decode(RegistryFriendlyByteBuf buffer) {

        int start = buffer.readerIndex();

        try {
            return delegate.decode(buffer);
        } catch (Throwable t) {
            if (REPORTED.compareAndSet(false, true)) {
                GoldenTweaks.LOGGER.warn(
                        "[WATUT 兼容] nbt_client 载荷解码失败，已丢弃该载荷以免掉线"
                                + "（起始偏移 {}，已读 {} 字节，线程 {}）；这本身是 WATUT 发出的 NBT 损坏，"
                                + "如果频繁出现请把这条和崩溃报告一起回贴",
                        start, buffer.readerIndex() - start, Thread.currentThread().getName(), t
                );
            }
            return new PacketNBTFromServer(new CompoundTag());
        }
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, PacketNBTFromServer value) {
        delegate.encode(buffer, value);
    }
}
