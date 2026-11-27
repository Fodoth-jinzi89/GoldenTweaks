package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.core.api.energy.EnergyNodeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 蒸汽隧道电力端口的单包大小：512 -> 8,192。
 *
 * <p>电网一拍最多发 64 个包，而该端口的包被写死为 512 EU，于是天花板是
 * {@code 512 × 64 = 32,768 EU/t}。流体倍率把涡轮机产出抬高之后，多出来的电会卡在这个
 * 上限上发不出去。改成 8,192 后天花板变成 {@code 8,192 × 64 = 524,288 EU/t}。</p>
 *
 * <p>做法与柴油发电机那次一致：在 {@code generatorPacket} 的 HEAD 直接给出定值，
 * 不去碰 {@code EnergyNodeProfile} 的构造。</p>
 */
@Mixin(value = EnergyNodeRegistry.class, remap = false)
public abstract class MioIcifSteamTunnelPacketMixin {

    private static final ResourceLocation STEAM_TUNNEL_POWER_PORT =
            ResourceLocation.fromNamespaceAndPath("mio_icif", "generator/block_steam_tunnel_power_port");

    /** 单包大小，配合 64 包/拍的上限得到 524,288 EU/t。 */
    private static final long PACKET = 8192L;

    @Inject(method = "generatorPacket", at = @At("HEAD"), cancellable = true)
    private static void gt$steamTunnelPacket(ResourceLocation blockId, long fallback,
                                             CallbackInfoReturnable<Long> cir) {
        if (STEAM_TUNNEL_POWER_PORT.equals(blockId)) {
            cir.setReturnValue(PACKET);
        }
    }
}
