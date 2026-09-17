package net.fodoth.skina.goldentweaks.mixin.fix.lzxnonefate;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * lzxnonefate 1.0.2 的两个事件类（{@code KeyEvent}、{@code EAEvent}，全 mod 只有这两个）
 * 都在 {@code onPlayerTickPost(PlayerTickEvent.Post)} 开头写了
 * {@code if (!(player instanceof LocalPlayer lp)) return;}。
 *
 * <p>PlayerTickEvent.Post 是**双端**事件，而 {@code instanceof} 指令会强制解析
 * {@code net.minecraft.client.player.LocalPlayer} ⇒ 专用服务器上每个玩家每次 tick 都抛
 * “Attempted to load class net/minecraft/client/player/LocalPlayer for invalid dist DEDICATED_SERVER”
 * ⇒ 异常冲出事件总线 → {@code ServerPlayer.doTick} 抛 ReportedException（该处无人 catch）
 * ⇒ 玩家连接被关，日志 “lost connection: Internal server error”（服务端本身不倒）。</p>
 *
 * <p>两个方法的结构都是“不是 LocalPlayer 就直接 return”，所以在服务端它们本来就是空操作，
 * 取消零副作用；同一批类里正确使用 {@code instanceof ServerPlayer} 的 {@code onPlayerTick}
 * （服务端状态机靠它 tick）保持原样，客户端 dist 也完全不动。</p>
 */
@Mixin(targets = {
        "com.lzxnone.fate.event.KeyEvent",
        "com.lzxnone.fate.event.EAEvent"
}, remap = false)
public abstract class FateEventMixin {

    @Inject(method = "onPlayerTickPost", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gt$skipClientOnlyTickOnServer(PlayerTickEvent.Post event, CallbackInfo ci) {

        if (!FMLEnvironment.dist.isClient()) {
            ci.cancel();
        }
    }
}
