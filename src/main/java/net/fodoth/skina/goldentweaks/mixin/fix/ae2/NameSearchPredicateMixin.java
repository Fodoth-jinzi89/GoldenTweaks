package net.fodoth.skina.goldentweaks.mixin.fix.ae2;

import appeng.api.stacks.AEKey;
import net.fodoth.skina.goldentweaks.compat.ae2.GTAe2NameCache;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * AE2 ME 终端搜索里最贵的一步：每个条目每次都要把显示名
 * {@code getDisplayName().getString().toLowerCase()} 重新算一遍。改成取缓存，见 {@link GTAe2NameCache}。
 *
 * <p>目标类 {@code appeng.client.gui.me.search.NameSearchPredicate} 是包私有的，所以用
 * {@code targets} 字符串指定；{@code test} 还有个合成的桥方法 {@code test(Object)}，
 * 因此在选择器里写全描述符，只命中真正的那个。</p>
 *
 * <p><b>刻意不碰的地方：</b>不取消/替换 {@code test} 本身。JECharacters 的拼音就是靠把
 * {@code String.contains} 换进这个方法里实现的，替换掉整个方法会让拼音静默失效。</p>
 */
@Mixin(targets = "appeng.client.gui.me.search.NameSearchPredicate", remap = false)
public abstract class NameSearchPredicateMixin {

    @Redirect(method = "test(Lappeng/menu/me/common/GridInventoryEntry;)Z",
            at = @At(value = "INVOKE",
                    target = "Lappeng/api/stacks/AEKey;getDisplayName()Lnet/minecraft/network/chat/Component;"))
    private static Component gt$cachedDisplayName(AEKey key) {
        return GTAe2NameCache.loweredDisplayName(key);
    }
}
