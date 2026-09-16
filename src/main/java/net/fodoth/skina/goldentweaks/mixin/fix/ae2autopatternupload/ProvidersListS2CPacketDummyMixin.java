package net.fodoth.skina.goldentweaks.mixin.fix.ae2autopatternupload;

import org.spongepowered.asm.mixin.Mixin;

/**
 * 空 mixin：唯一作用是让 mixin 插件对 {@code ProvidersListS2CPacket} 调用 preApply，
 * 由 {@code Ae2ApuASM} 在专用服务器上清掉方法体里的客户端引用（否则注册包时链接该类会加载 Screen）。
 */
@Mixin(targets = "com.gali.ae2_auto_pattern_upload.network.ProvidersListS2CPacket", remap = false)
public abstract class ProvidersListS2CPacketDummyMixin {
}
