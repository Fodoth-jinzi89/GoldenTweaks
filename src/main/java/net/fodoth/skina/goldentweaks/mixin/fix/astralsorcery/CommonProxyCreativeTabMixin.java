package net.fodoth.skina.goldentweaks.mixin.fix.astralsorcery;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Astral Sorcery 2.0.1 把 {@code CreativeTabItem#fillCreativeTab(Consumer)} 标成了
 * {@code @OnlyIn(Dist.CLIENT)}，NeoForge 的 RuntimeDistCleaner 会在专用服务器上把这个默认方法
 * 整个删掉；但 {@code CommonProxy#onBuildCreativeTabContents} 没有做 dist 守卫，而
 * {@code BuildCreativeModeTabContentsEvent} 在服务端也会触发（原版在数据包重载时构建，本整合包里
 * ComputerCraft 的 {@code onServerStarted} 也会主动调一次）。
 *
 * <p>结果：AS 遍历自己的物品挨个调用该方法 ⇒ {@code NoSuchMethodError} ⇒ 异常冲出 CREATIVE_TAB
 * 注册事件 ⇒ “Astral Sorcery 在 BuildCreativeModeTabContentsEvent 事件调度时发生错误” ⇒ 服务器崩溃。</p>
 *
 * <p>创意模式标签内容是客户端各自构建的，服务端这份数据不参与同步，所以这里在专用服务器上直接
 * 取消该处理器；客户端保持原样。上游已在后续版本移除该 {@code @OnlyIn}。</p>
 */
@Mixin(targets = "hellfirepvp.astralsorcery.common.CommonProxy", remap = false)
public abstract class CommonProxyCreativeTabMixin {

    @Inject(method = "onBuildCreativeTabContents", at = @At("HEAD"), cancellable = true, remap = false)
    private void gt$skipCreativeTabOnDedicatedServer(BuildCreativeModeTabContentsEvent event, CallbackInfo ci) {

        if (FMLEnvironment.dist.isDedicatedServer()) {
            ci.cancel();
        }
    }
}
