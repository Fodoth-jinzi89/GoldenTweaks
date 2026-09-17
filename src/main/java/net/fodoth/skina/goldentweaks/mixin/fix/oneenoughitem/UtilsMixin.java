package net.fodoth.skina.goldentweaks.mixin.fix.oneenoughitem;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * OEI 1.0.8 的 {@code Utils#isTagExists(ResourceLocation, HolderLookup.RegistryLookup)} 直接对传入的
 * {@code registryLookup} 调 {@code .get(tagKey)}。客户端进服时 OEI 的数据同步
 * （oelib {@code DataSyncChunkPacket} → {@code onDataReload} → {@code ServerEventHandler.rebuildReplacementCache}）
 * 早于注册表就绪，传进来的是 null ⇒
 * {@code NullPointerException: Cannot invoke "HolderLookup$RegistryLookup.get(TagKey)" because "registryLookup" is null}
 * （客户端日志里每次进服刷一次，事件总线把它 catch 了，但同步逻辑就此中断）。
 *
 * <p>查不到注册表就无从判断 tag 是否存在 ⇒ 直接返回 false（该替换规则视为无效），
 * 注册表正常时行为完全不变。</p>
 */
@Mixin(targets = "com.mafuyu404.oneenoughitem.util.Utils", remap = false)
public abstract class UtilsMixin {

    @Inject(method = "isTagExists", at = @At("HEAD"), cancellable = true)
    private static void gt$skipNullRegistryLookup(
            ResourceLocation id,
            HolderLookup.RegistryLookup<Item> registryLookup,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (registryLookup == null) {
            cir.setReturnValue(false);
        }
    }
}
