package net.fodoth.skina.goldentweaks.mixin.fix.appliedkjs;

import org.spongepowered.asm.mixin.Mixin;

/**
 * 空 mixin：唯一作用是让 mixin 插件对 {@code AppliedKJSServerState} 调用 preApply，
 * 由 {@code AppliedKjsASM} 把其跨线程共享的 HashMap/HashSet 换成并发实现，
 * 修复区块加载线程与服务器 tick 线程并发访问导致的 ConcurrentModificationException。
 */
@Mixin(targets = "com.rollylindenshnizzer.appliedkjs.AppliedKJSServerState", remap = false)
public abstract class AppliedKjsServerStateDummyMixin {
}
