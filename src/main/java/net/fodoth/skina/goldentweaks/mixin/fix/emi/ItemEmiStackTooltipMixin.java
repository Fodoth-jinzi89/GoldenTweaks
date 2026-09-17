package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.api.stack.ItemEmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * EMI 1.1.24 的 {@code ItemEmiStack#getTooltipText()} 会无条件走 {@code client.level.registryAccess()}
 * 去解析物品组件/词条；而 EMI **没有进入世界时也会烘焙搜索索引**（资源重载，或退出世界后
 * TMRV 发 onRuntimeUnavailable → EMI 重载），此时 {@code client.level} 为 null
 * ⇒ 每个需要注册表的条目都抛一次 NPE。
 *
 * <p>实测（latest - 副本.log）：一次无世界的烘焙刷了 15329 条
 * “EMI caught an exception while baking search …”，整次重载被拖到 105831ms（正常的几次约 40s），
 * 日志因此涨到 129MB。</p>
 *
 * <p>这里只在**没有世界**时提前返回空 tooltip：搜索索引少一段词条文本，进世界后 EMI 会重新烘焙补上；
 * 有世界时行为完全不变。</p>
 */
@Mixin(value = ItemEmiStack.class, remap = false)
public abstract class ItemEmiStackTooltipMixin {

    @Inject(method = "getTooltipText", at = @At("HEAD"), cancellable = true)
    private void gt$skipTooltipWithoutLevel(CallbackInfoReturnable<List<Component>> cir) {

        if (Minecraft.getInstance().level == null) {
            cir.setReturnValue(List.of());
        }
    }
}
