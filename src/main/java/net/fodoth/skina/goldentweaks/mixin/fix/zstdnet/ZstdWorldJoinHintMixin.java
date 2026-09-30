package net.fodoth.skina.goldentweaks.mixin.fix.zstdnet;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 关闭 zstdnet 进入单人世界后在聊天栏发送的两条 zstd 端口提示
 * （{@code zstdnet.singleplayer.lan_hint} / {@code zstdnet.singleplayer.lan_command_hint}）。
 * <p>
 * 这两条提示由 {@code onClientTick} 独占处理，整个方法只做「首次进入单人世界时发一次提示」，
 * 因此在方法头取消即可，不影响其它逻辑。
 * <p>
 * {@code targets} 用类名寻址，避免编译期依赖 zstdnet；{@code require = 0} 让方法改名后只失效不崩溃。
 */
@Mixin(targets = "cn.tohsaka.factory.zstdnet.client.ClientProxyPublisher", remap = false)
public class ZstdWorldJoinHintMixin {

    @Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true, require = 0)
    private void gt$disableWorldJoinHint(ClientTickEvent.Post event, CallbackInfo ci) {
        ci.cancel();
    }
}
