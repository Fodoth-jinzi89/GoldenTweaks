package net.fodoth.skina.goldentweaks.mixin.fix.linearbearing;

import org.spongepowered.asm.mixin.Mixin;

/**
 * 空 mixin：唯一作用是让 mixin 插件对 {@code LinearBearing} 调用 preApply，
 * 由 {@code LinearbearingASM} 在专用服务器上删掉客户端专属监听器注册。
 */
@Mixin(targets = "com.bearing.linearbearing.LinearBearing", remap = false)
public abstract class LinearBearingDummyMixin {
}
