package net.fodoth.skina.goldentweaks.mixin.fix.lzxnonefate;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * lzxnonefate 1.0.2 的 {@code KeyEvent.onPlayerTickPost(PlayerTickEvent.Post)} 里写了
 * {@code player instanceof LocalPlayer}。PlayerTickEvent.Post 是**双端**事件，
 * 而 {@code instanceof} 会强制解析 {@code net.minecraft.client.player.LocalPlayer}
 * ⇒ 专用服务器上每个玩家每次 tick 都抛
 * “Attempted to load class net/minecraft/client/player/LocalPlayer for invalid dist DEDICATED_SERVER”
 * ⇒ 异常冲出事件总线 → {@code ServerPlayer.doTick} 抛 ReportedException → 玩家一进服就被
 * “Internal server error” 踢掉（服务端本身不倒）。
 *
 * <p>该 handler 的动作全在 {@code instanceof LocalPlayer} 分支里，服务端上本来就是空操作，
 * 所以只在 DEDICATED_SERVER 上取消它——同类的 {@code onPlayerTick}（用 ServerPlayer 分支的那个）
 * 保持原样，服务端的武器状态机照常 tick。</p>
 */
@Mixin(targets = "com.lzxnone.fate.event.KeyEvent", remap = false)
public abstract class KeyEventMixin {

    @Inject(method = "onPlayerTickPost", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gt$skipClientOnlyTickOnServer(PlayerTickEvent.Post event, CallbackInfo ci) {

        if (!FMLEnvironment.dist.isClient()) {
            ci.cancel();
        }
    }
}
